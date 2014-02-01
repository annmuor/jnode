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

	public static BinkpAbstractConnector connect(String cmd) {

		try {
			Process process = Runtime.getRuntime().exec(cmd);
			BinkpPipeConnector pipe = new BinkpPipeConnector();
			pipe.clientConnection = true;
			pipe.process = process;
			return pipe;
		} catch (IOException e) {
			logger.l2("Pipe exec error: " + e.getLocalizedMessage());
			return null;
		}
	}

	private Process process;
	private volatile boolean closed = false;

	public BinkpPipeConnector() {
		init();
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
				while (!closed) {
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
						}
					}
				}
				logger.l4("processOutputObserver exits");
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
