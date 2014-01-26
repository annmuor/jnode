package jnode.protocol.binkp;

import static jnode.protocol.binkp.BinkpProtocolTools.getCommand;
import static jnode.protocol.binkp.BinkpProtocolTools.write;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import jnode.event.ConnectionEndEvent;
import jnode.event.Notifier;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.main.threads.PollQueue;

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

	private BinkpAsyncConnector(SocketChannel socket, boolean clientConnection)
			throws IOException {
		this.clientConnection = clientConnection;
		socket.configureBlocking(false);
		selector = Selector.open();
		socket.register(selector, socket.validOps());
	}

	@Override
	public void run() {
		int remains = 0;
		int len = 0;
		int head_len = 0;
		int head_remains = 0;
		boolean command = false;
		ByteBuffer buffer = null;
		ByteBuffer headBuf = null;
		long lastActive = 0;
		try {
			greet();
			while (true) {

				long now = new Date().getTime();
				if (lastActive != 0) {
					if (now - lastActive > staticMaxTimeout) {
						throw new ConnectionEndException();
					}
				}
				try {
					checkEOB();
					selector.selectedKeys().clear();
					selector.select();
					for (SelectionKey key : selector.selectedKeys()) {
						SocketChannel channel = (SocketChannel) key.channel();
						if (lastActive == 0) {
							InetSocketAddress addr = (InetSocketAddress) channel
									.getRemoteAddress();
							logger.l2(String.format("Connected with %s:%d",
									addr.getHostString(), addr.getPort()));
							lastActive = new Date().getTime();
						}
						if (key.isValid()) {
							if (key.isConnectable()) {
								if (!channel.finishConnect()) {
									key.cancel();
								}
							}
							if (key.isReadable()) {
								// read frame
								BinkpFrame frame = null;
								try {
									if (len == 0) {
										if (head_len == 0) {
											headBuf = ByteBuffer.allocate(2);
											head_len = 2;
											head_remains = 2;
										}
										int n = readOrDie(headBuf, channel);
										head_remains = head_len - n;
										if (head_remains > 0) {
											continue;
										}
										headBuf.flip();
										int head = headBuf.getShort() & 0xFFFF;
										command = ((head >> 15) == 1);
										len = head & 0x7FFF;
										headBuf = null;
										remains = len;
										buffer = ByteBuffer.allocate(len);
										buffer.clear();
										head_len = 0;
									}
									if (len > 0) {
										int n = readOrDie(buffer, channel);
										remains -= n;
										if (remains > 0) {
											continue;
										}
										buffer.flip();
										if (command) {
											int cmd = buffer.get();
											cmd &= 0xFF;
											String arg = null;
											if (len > 1) {
												int next_len = buffer
														.remaining();
												if (buffer.get(len - 1) == 0) {
													next_len--;
												}
												byte[] buf = new byte[next_len];
												buffer.get(buf, 0, next_len);
												arg = new String(buf);
											}
											frame = new BinkpFrame(
													getCommand(cmd), arg);
										} else {
											frame = new BinkpFrame(
													buffer.array());
										}

										len = 0;
										remains = 0;
										buffer = null;
									}
								} catch (IOException e) {
									error("Unable to read frame");
									frame = null;
								}
								if (frame != null) {
									logger.l5("Frame received: " + frame);
									proccessFrame(frame);
									lastActive = new Date().getTime();
								}
							}
							if (key.isWritable()) {
								checkForMessages();
								if (!frames.isEmpty()) {
									BinkpFrame frame = frames.removeFirst();
									logger.l5("Frame sent: " + frame);
									write(frame, channel);
									lastActive = new Date().getTime();
								} else {
									if (connectionState == STATE_END
											|| connectionState == STATE_ERROR) {
										finish();
									}
								}
							}
						} else {
							throw new ConnectionEndException();
						}
					}

				} catch (IOException e) {
					error("IOException");
					throw new ConnectionEndException();
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
				if (transferringMessage != null) {
					transferringMessage.getInputStream().close();
				}
			} catch (IOException e2) {
				logger.l2("Error while closing key", e2);
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
			end();
			Notifier.INSTANSE.notify(event);
		}
	}

	private int readOrDie(ByteBuffer buffer, SocketChannel channel)
			throws IOException {
		int x = channel.read(buffer);
		if (x == -1) {
			throw new ConnectionEndException("Connection reset by peer");
		}
		return x;
	}
}
