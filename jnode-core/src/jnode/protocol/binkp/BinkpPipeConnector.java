package jnode.protocol.binkp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import jnode.event.ConnectionEndEvent;
import jnode.event.Notifier;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.main.threads.PollQueue;

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
					if (frames.isEmpty()) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					} else {
						BinkpFrame frame = frames.removeFirst();
						try {
							process.getOutputStream().write(frame.getBytes());
							process.getOutputStream().flush();
							logger.l5("Frame sent: " + frame);
						} catch (IOException e) {
							logger.l2("IOException: " + e.getLocalizedMessage());
							break;
						}
					}
				}
				logger.l4("processOutputObserver exits");
				closed = true;
				return;
			}
		};
		new Thread(processOutputObserver).start();
		try {
			greet();
			while (!closed) {
				checkEOB();
				checkForMessages();
				if (connectionState == STATE_END
						|| connectionState == STATE_ERROR) {
					finish();
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
					byte[] ndata = new byte[len - 1];
					data.get(ndata);
					frame = new BinkpFrame(cmd, new String(ndata));
				} else {
					frame = new BinkpFrame(data.array());
				}
				logger.l5("Frame received: " + frame);
				proccessFrame(frame);
			}
			finish();
		} catch (ConnectionEndException e) {
			try {
				Thread.sleep(100); // let's proccess to write messages;
			} catch (InterruptedException ignore) {
			}
			closed = true;
			logger.l5("Connection end: " + e.getLocalizedMessage());
			process.destroy();
			try {
				if (currentOS != null) {
					currentOS.close();
				}
				if (transferringMessage != null) {
					transferringMessage.getInputStream().close();
				}
			} catch (IOException ignore) {
			}
			ConnectionEndEvent event = null;
			if (!foreignAddress.isEmpty()) {
				for (FtnAddress addr : foreignAddress) {
					PollQueue.getSelf().end(addr);
				}
				String address = (foreignLink != null) ? foreignLink
						.getLinkAddress() : foreignAddress.get(0).toString();
				logger.l3(String
						.format((connectionState == STATE_END) ? "Done, Sb/Rb: %d/%d (%s)"
								: "Done with errors, Sb/Rb: %d/%d (%s)",
								total_sent_bytes, total_recv_bytes, address));
				event = new ConnectionEndEvent(new FtnAddress(address),
						!clientConnection, (connectionState == STATE_END),
						total_recv_bytes, total_sent_bytes);
			} else {
				event = new ConnectionEndEvent(clientConnection, false);
				logger.l3("Connection ended as unknown");
			}
			Notifier.INSTANSE.notify(event);
		}

	}

	private int readOrDie(InputStream inputStream) {
		try {
			int x = inputStream.read();
			if (x == -1) {
				finish();
			}
			return x;
		} catch (IOException e) {
			finish();
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
				finish();
			}
			ByteBuffer ret = ByteBuffer.wrap(buf, 0, x);
			return ret.array();
		} catch (IOException e) {
			finish();
			return new byte[] {};
		}
	}
}
