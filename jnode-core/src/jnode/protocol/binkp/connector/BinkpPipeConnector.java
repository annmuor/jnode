/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.protocol.binkp.connector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import jnode.logger.Logger;
import jnode.main.threads.ThreadPool;
import jnode.protocol.binkp.BinkpProtocolTools;
import jnode.protocol.binkp.exceprion.ConnectionEndException;
import jnode.protocol.binkp.types.BinkpCommand;
import jnode.protocol.binkp.types.BinkpFrame;

/**
 * Соединение через пайп
 * 
 * @author kreon
 * 
 */
public class BinkpPipeConnector extends BinkpAbstractConnector {
	static final Logger logger = Logger.getLogger(BinkpPipeConnector.class);

	private volatile Process process;
	private volatile boolean closed = false;
	private volatile boolean connected = true;

	public BinkpPipeConnector(String protocolAddress) throws IOException {
		super(protocolAddress);
		process = Runtime.getRuntime().exec(protocolAddress);
	}

	/**
	 * :-)
	 */
	@Override
	public void run() {
		Runnable processOutputObserver = new Runnable() {

			@Override
			public void run() {
				logger.l4("processOutputObserver started");
				boolean last = false;
				while (connected) {
					connected = isConnected(); // SHIT VM Optimisation
					if (process == null || last) {
						// last iteration & exit
						break;
					}
					if (closed) {
						last = true;
					}
					checkForMessages();
					if (frames.isEmpty()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					} else {
						try {
							BinkpFrame frame = frames.removeFirst();
							try {
								process.getOutputStream().write(
										frame.getBytes());
								process.getOutputStream().flush();
								logger.l5("Frame sent: " + frame);
							} catch (IOException e) {
								logger.l2("IOException: "
										+ e.getLocalizedMessage());
								break;
							}
						} catch (NoSuchElementException ignore) {
							logger.l3("NoSuchElement exception");
						}
					}
				}
				logger.l3("PIPE processOutputObserver exits");
				closed = true;
				return;
			}
		};
		ThreadPool.execute(processOutputObserver);
		try {
			greet();
			while (!closed) {
				if (!isConnected()) {
					try {
						Thread.sleep(100); // let's proccess to write messages;
					} catch (InterruptedException ignore) {
					}
					continue;
				}
				int[] head = new int[2];
				for (int i = 0; i < 2; i++) {
					head[i] = readOrDie(process.getInputStream());
				}
				int len = ((head[0] & 0xff) << 8 | (head[1] & 0xff)) & 0x7FFF;
				int remaining = len;
				ByteBuffer data = ByteBuffer.allocate(len);
				boolean command = (head[0] & 0x80) > 0;
				while (remaining > 0) {
					byte[] buf = readOrDie(process.getInputStream(), remaining);
					remaining -= buf.length;
					data.put(buf);
				}
				data.flip();
				BinkpFrame frame;
				if (command) {
					BinkpCommand cmd = BinkpProtocolTools
							.getCommand(data.get());
					if (data.get(len - 1) == 0) {
						len--;
					}
					byte[] ndata = new byte[len - 1];
					data.get(ndata);
					frame = new BinkpFrame(cmd, new String(ndata));
				} else {
					frame = new BinkpFrame(data.array());
				}
				logger.l5("Frame received: " + frame);
				proccessFrame(frame);
			}
			finish("Connection closed");
		} catch (ConnectionEndException e) {
			try {
				Thread.sleep(100); // let's proccess to write messages;
			} catch (InterruptedException ignore) {
			}
			closed = true;
			logger.l5("Connection end: " + e.getLocalizedMessage());
			process.destroy();
			process = null;
			done();
		}

	}

	private int readOrDie(InputStream inputStream) {
		try {
			int x = inputStream.read();
			if (x == -1) {
				if (flag_leob && flag_reob) {
					connectionState = STATE_END;
				}
				finish("readOrDie(1) EOF");
			}
			return x;
		} catch (IOException e) {
			if (flag_leob && flag_reob) {
				connectionState = STATE_END;
			}
			finish("readOrDie(1) Exception");
			return -1;
		}
	}

	private byte[] readOrDie(InputStream inputStream, int remaining) {
		try {
			int len = (remaining > staticBufMaxSize) ? staticBufMaxSize
					: remaining;
			byte[] buf = new byte[len];
			int x = inputStream.read(buf);
			if (x == -1) {
				if (flag_leob && flag_reob) {
					connectionState = STATE_END;
				}
				finish("readOrDie(2) EOF");
			}
			ByteBuffer ret = ByteBuffer.wrap(buf, 0, x);
			return ret.array();
		} catch (IOException e) {
			finish("readOrDie(2) Exception");
			return new byte[] {};
		}
	}
}
