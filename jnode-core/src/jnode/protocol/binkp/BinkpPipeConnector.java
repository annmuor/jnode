package jnode.protocol.binkp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import jnode.logger.Logger;

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

	@Override
	public void run() {
		Runnable processOutputObserver = new Runnable() {

			@Override
			public void run() {
				while (!closed) {
					while (frames.isEmpty()) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
					BinkpFrame frame = frames.removeFirst();
					try {
						process.getOutputStream().write(frame.getBytes());
						process.getOutputStream().flush();
					} catch (IOException e) {
						logger.l3("IOException: " + e.getLocalizedMessage());
						break;
					}
				}
				closed = true;
				return;
			}
		};
		new Thread(processOutputObserver).start();

		try {
			while (!closed) {
				int[] head = new int[2];
				for (int i = 0; i < 2; i++) {
					head[i] = readOrDie(process.getInputStream());
				}
				int len = (head[0] & 0xff << 8 | head[1] & 0xff) & 0x7FFF;
				int remaining = len;
				ByteBuffer data = ByteBuffer.allocate(len);
				boolean command = (head[0] & 0x80) > 0;
				while (remaining > 0) {
					byte[] buf = readOrDieB(process.getInputStream());
					data.put(buf);
					remaining -= buf.length;
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
			throw new ConnectionEndException("Closed by others");
		} catch (ConnectionEndException e) {
			closed = true;
			logger.l2("Connection end: " + e.getLocalizedMessage());
			// TODO: stats
		}

	}

	private int readOrDie(InputStream inputStream) {
		try {
			int x = inputStream.read();
			if (x == -1) {
				throw new ConnectionEndException("InputStream EOF");
			}
			return x;
		} catch (IOException e) {
			throw new ConnectionEndException("InputStream exception: "
					+ e.getLocalizedMessage(), e);
		}
	}

	private byte[] readOrDieB(InputStream inputStream) {
		try {
			byte[] buf = new byte[staticBufMaxSize];
			int x = inputStream.read(buf);
			if (x == -1) {
				throw new ConnectionEndException("InputStream EOF");
			}
			ByteBuffer ret = ByteBuffer.wrap(buf, 0, x);
			return ret.array();
		} catch (IOException e) {
			throw new ConnectionEndException("InputStream exception: "
					+ e.getLocalizedMessage(), e);
		}
	}
}
