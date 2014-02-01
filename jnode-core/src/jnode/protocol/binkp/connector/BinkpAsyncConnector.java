package jnode.protocol.binkp.connector;

import static jnode.protocol.binkp.BinkpProtocolTools.getCommand;
import static jnode.protocol.binkp.BinkpProtocolTools.write;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import jnode.logger.Logger;
import jnode.protocol.binkp.exceprion.ConnectionEndException;
import jnode.protocol.binkp.types.BinkpCommand;
import jnode.protocol.binkp.types.BinkpFrame;

/**
 * TCP/IP соединение
 * 
 * @author kreon
 * 
 */
public class BinkpAsyncConnector extends BinkpAbstractConnector {
	static final Logger logger = Logger.getLogger(BinkpAsyncConnector.class);

	public static BinkpAbstractConnector connect(String host, Integer port) {
		init();
		SocketChannel socket = null;
		try {
			socket = SocketChannel.open();
			socket.connect(new InetSocketAddress(host, port));
			return new BinkpAsyncConnector(socket, true);
		} catch (IOException e) {
			logger.l1("Connect error: " + e.getMessage());
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ignore) {
				}
			}
			return null;
		}
	}

	public static BinkpAbstractConnector accept(SocketChannel socket) {
		init();
		try {
			return new BinkpAsyncConnector(socket, false);
		} catch (IOException e) {
			logger.l1("Accept error", e);
			return null;
		}
	}

	private Selector selector;

	private BinkpAsyncConnector(SocketChannel socket, boolean clientConnection)
			throws IOException {
		this.clientConnection = clientConnection;
		socket.configureBlocking(false);
		selector = Selector.open();
		socket.register(selector, socket.validOps());
	}

	@Override
	public void run() {
		try {
			greet();
			while (true) {
				try {
					selector.select(staticMaxTimeout);
					for (SelectionKey key : selector.selectedKeys()) {
						SocketChannel channel = (SocketChannel) key.channel();
						if (key.isValid()) {
							if (key.isConnectable()) {
								if (!channel.finishConnect()) {
									key.cancel();
									finish("Connect failed");
								} else {
									InetSocketAddress addr = (InetSocketAddress) channel
											.getRemoteAddress();
									logger.l2(String.format(
											"Connected with %s:%d",
											addr.getHostString(),
											addr.getPort()));
								}
							}
							if (key.isWritable()) {
								checkForMessages();
								if (!frames.isEmpty()) {
									BinkpFrame frame = frames.removeFirst();
									logger.l5("Frame sent: " + frame
											+ ", next " + frames.size()
											+ " frames, total sent "
											+ total_sent_bytes);
									write(frame, channel);
								}
							}
							if(!isConnected()) {
								finish("Connect ended");
							}
							if (key.isReadable()) {
								BinkpFrame frame = null;
								ByteBuffer head = ByteBuffer.allocate(2);
								for (int len = 0; len < 2;) {
									len += readOrDie(head, channel);
								}
								head.flip();
								int header = ((int) head.getShort()) & 0xffff;
								int datalen = header & 0x7fff;
								ByteBuffer data = ByteBuffer.allocate(datalen);
								for (int len = 0; len < datalen;) {
									len += readOrDie(data, channel);
								}
								data.flip();
								if ((header & 0x8000) >= 0x8000) {
									// command
									BinkpCommand cmd = getCommand(data.get());
									if (datalen > 1) {
										if(data.get(datalen-1) == 0) {
											datalen--;
										}
										byte[] buf = new byte[datalen-1];
										data.get(buf);
										frame = new BinkpFrame(cmd, new String(
												buf));
									} else {
										frame = new BinkpFrame(cmd);
									}
								} else {
									frame = new BinkpFrame(data.array(),
											datalen);
								}
								if (frame != null) {
									logger.l5("Frame received: " + frame);
									proccessFrame(frame);
								}
							}
						} else {
							finish("Key is invalid");
						}
					}
				} catch (IOException e) {
					error("IOException");
					
				}
			}
		} catch (ConnectionEndException e) {
			try {
				for (SelectionKey key : selector.keys()) {
					key.channel().close();
					key.cancel();
				}
				selector.close();
				if (currentOS != null) {
					currentOS.close();
				}
			} catch (IOException e2) {
				logger.l2("Error while closing key", e2);
			}
			done();
		}
	}

	private int readOrDie(ByteBuffer buffer, SocketChannel channel)
			throws IOException {
		int x = channel.read(buffer);
		if (x == -1) {
			if (flag_leob && flag_reob) {
				connectionState = STATE_END;
			}
			finish("readOrDie failed");
		}
		return x;
	}
}
