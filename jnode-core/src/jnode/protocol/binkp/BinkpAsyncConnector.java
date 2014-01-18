package jnode.protocol.binkp;

import static jnode.protocol.binkp.BinkpProtocolTools.createMessage;
import static jnode.protocol.binkp.BinkpProtocolTools.forwardToTossing;
import static jnode.protocol.binkp.BinkpProtocolTools.getAuthPassword;
import static jnode.protocol.binkp.BinkpProtocolTools.getCommand;
import static jnode.protocol.binkp.BinkpProtocolTools.getString;
import static jnode.protocol.binkp.BinkpProtocolTools.messageEquals;
import static jnode.protocol.binkp.BinkpProtocolTools.write;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.event.ConnectionEndEvent;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.ftn.tosser.FtnTosser;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.SystemInfo;
import jnode.main.threads.ThreadPool;
import jnode.ndl.NodelistScanner;
import jnode.protocol.io.Message;

/**
 * Версия 2.0 так сказать :-)
 * 
 * @author kreon
 * 
 */
public class BinkpAsyncConnector implements Runnable {
	private static final Logger logger = Logger
			.getLogger(BinkpAsyncConnector.class);
	private static final DateFormat format = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	private static final Pattern cramPattern = Pattern
			.compile("^CRAM-([-A-Z0-9]+)-([a-f0-9]+)$");

	private static final int STATE_GREET = 0;
	private static final int STATE_ERROR = 1;
	private static final int STATE_END = 2;
	private static final int STATE_ADDR = 3;
	private static final int STATE_AUTH = 4;
	private static final int STATE_TRANSFER = 5;

	private static final String BINKP_NETWORK_NAME = "binkp.network";
	private static final String BINKP_MAX_MEM = "binkp.maxmem";
	private static final String BINKP_TEMP = "bink.temp";
	private static final String BINKP_SIZE = "binkp.size";
	private static final String BINKP_TIMEOUT = "binkp.timeout";

	private static Integer staticMemMaxSize = null;
	private static Integer staticBufMaxSize = null;
	private static File staticTempDirectory = null;
	private static String staticNetworkName = null;
	private static Long staticMaxTimeout = null;

	private static void init() {
		if (staticTempDirectory == null) {
			staticTempDirectory = new File(MainHandler.getCurrentInstance()
					.getProperty(BINKP_TEMP,
							System.getProperty("java.io.tmpdir")));
		}
		if (staticNetworkName == null) {
			staticNetworkName = MainHandler.getCurrentInstance().getProperty(
					BINKP_NETWORK_NAME, "fidonet");
		}
		if (staticMemMaxSize == null) {
			staticMemMaxSize = MainHandler.getCurrentInstance()
					.getIntegerProperty(BINKP_MAX_MEM, 10485760);
		}
		if (staticBufMaxSize == null) {
			staticBufMaxSize = MainHandler.getCurrentInstance()
					.getIntegerProperty(BINKP_SIZE, 32767);
			if (staticBufMaxSize > 32767) {
				staticBufMaxSize = 32767;
			}
		}
		if (staticMaxTimeout == null) {
			staticMaxTimeout = (long) MainHandler.getCurrentInstance()
					.getIntegerProperty(BINKP_TIMEOUT, 30);
			staticMaxTimeout *= 1000;
		}
	}

	public static BinkpAsyncConnector connect(String host, Integer port) {
		init();
		try {
			SocketChannel socket = SocketChannel.open();
			socket.connect(new InetSocketAddress(host, port));
			return new BinkpAsyncConnector(socket, true);
		} catch (IOException e) {
			logger.l1("Connect error: " + e.getMessage());
			return null;
		}
	}

	public static BinkpAsyncConnector accept(SocketChannel socket) {
		init();
		try {
			return new BinkpAsyncConnector(socket, false);
		} catch (IOException e) {
			logger.l1("Accept error", e);
			return null;
		}
	}

	private int connectionState = STATE_GREET;
	private List<FtnAddress> foreignAddress = new ArrayList<>();
	private List<FtnAddress> ourAddress = new ArrayList<>();
	private Link foreignLink;
	private boolean secure = false;
	private boolean clientConnection = true;
	private String cramAlgo = null;
	private String cramText = null;
	private boolean binkp1_0 = true;
	private LinkedList<Message> messages = new LinkedList<>();
	private Message transferringMessage = null;
	private Message receivingMessage = null;

	private File currentFile;
	private OutputStream currentOS;
	private long receivingBytesLeft;

	private boolean flag_leob = false;
	private boolean flag_reob = false;
	private int sent_bytes = 0;
	private int recv_bytes = 0;

	private int total_sent_bytes = 0;
	private int total_recv_bytes = 0;

	private Selector selector;

	private LinkedList<BinkpFrame> frames = new LinkedList<>();

	private BinkpAsyncConnector(SocketChannel socket, boolean clientConnection)
			throws IOException {
		this.clientConnection = clientConnection;
		socket.configureBlocking(false);
		selector = Selector.open();
		socket.register(selector, socket.validOps());
		InetSocketAddress addr = (InetSocketAddress) socket.getRemoteAddress();
		logger.l2(String.format("Connected with %s:%d", addr.getHostString(),
				addr.getPort()));
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
		long lastActive = new Date().getTime();
		greet();

		while (true) {
			try {
				long now = new Date().getTime();
				if (now - lastActive > staticMaxTimeout) {
					throw new ConnectionEndException();
				}
				try {
					if (flag_leob && flag_reob) {
						total_recv_bytes += recv_bytes;
						total_sent_bytes += sent_bytes;
						if (sent_bytes + recv_bytes == 0 || binkp1_0) {
							finish();
						} else {
							flag_leob = false;
							flag_reob = false;
							sent_bytes = 0;
							recv_bytes = 0;
						}
					}
					selector.selectedKeys().clear();
					selector.select();
					for (SelectionKey key : selector.selectedKeys()) {
						SocketChannel channel = (SocketChannel) key.channel();
						if (!channel.isConnected() || !channel.isOpen()) {
							throw new ConnectionEndException();
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
										int n = channel.read(headBuf);
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
										int n = channel.read(buffer);
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
								while (!frames.isEmpty()) {
									BinkpFrame frame = frames.removeFirst();
									logger.l5("Frame sent: " + frame);
									write(frame, channel);
									lastActive = new Date().getTime();
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
			} catch (ConnectionEndException e) {
				ConnectionEndEvent event = null;
				if (!foreignAddress.isEmpty()) {
					String address = (foreignLink != null) ? foreignLink
							.getLinkAddress() : foreignAddress.get(0)
							.toString();
					logger.l3(String
							.format((connectionState == STATE_END) ? "Done, Sb/Rb: %d/%d (%s)"
									: "Done with errors, Sb/Rb: %d/%d (%s)",
									total_sent_bytes, total_recv_bytes, address));
					event = new ConnectionEndEvent(new FtnAddress(address),
							clientConnection, (connectionState == STATE_END),
							total_recv_bytes, total_sent_bytes);
				} else {
					event = new ConnectionEndEvent(clientConnection, false);
					logger.l3("Connection ended as unknown");
				}
				Notifier.INSTANSE.notify(event);
				break;
			}
		}
	}

	private void error(String text) {
		frames.addLast(new BinkpFrame(BinkpCommand.M_ERR, text));
		logger.l2("Local error: " + text);
		connectionState = STATE_ERROR;
		throw new ConnectionEndException();
	}

	private void proccessFrame(BinkpFrame frame) {
		if (frame.isCommand()) {
			switch (frame.getCommand()) {
			case M_NUL:
				m_null(frame.getArg());
				break;
			case M_ADR:
				m_adr(frame.getArg());
				break;
			case M_PWD:
				m_pwd(frame.getArg());
				break;
			case M_OK:
				m_ok();
				break;
			case M_ERR:
				rerror("Remote told: " + frame.getArg());
				break;

			case M_EOB:
				m_eob();
				break;

			case M_FILE:
				m_file(frame.getArg());
				break;

			case M_GOT:
				m_got(frame.getArg());
				break;

			case M_GET:
				m_get(frame.getArg());
				break;

			case M_SKIP:
				m_skip(frame.getArg());
				break;

			case M_BSY:
				m_bsy(frame.getArg());

			default:
				break;
			}
		} else {
			if (receivingMessage != null) {
				if (receivingBytesLeft > 0) {
					byte[] data = frame.getData();
					int len = data.length;
					try {
						if (receivingBytesLeft >= len) {
							currentOS.write(data);
							receivingBytesLeft -= len;
						} else {
							currentOS.write(data, 0, (int) receivingBytesLeft);
							receivingBytesLeft = 0;
						}
						recv_bytes += len;
					} catch (IOException e) {
						frames.addLast(new BinkpFrame(BinkpCommand.M_SKIP,
								getString(receivingMessage)));
						receivingMessage = null;
						receivingBytesLeft = 0;
					}
				} else {
					logger.l4("Unknown data frame " + frame);
				}
				if (receivingBytesLeft == 0) {
					try {
						currentOS.close();
					} catch (IOException e) {
					}
					int ret = forwardToTossing(receivingMessage, currentFile,
							currentOS);
					frames.addLast(new BinkpFrame(
							(ret == 0) ? BinkpCommand.M_GOT
									: BinkpCommand.M_SKIP,
							getString(receivingMessage)));
					logger.l3(String.format("Received file: %s (%d)",
							receivingMessage.getMessageName(),
							receivingMessage.getMessageLength()));
					receivingMessage = null;
					receivingBytesLeft = 0;
					currentFile = null;
					currentOS = null;

				}
			} else {
				logger.l4("Unknown data frame: " + frame);
			}
		}

	}

	private void m_bsy(String arg) {
		logger.l3("Remote is busy: " + arg);
		throw new ConnectionEndException();
	}

	private void rerror(String string) {
		logger.l2("Remote error: " + string);
		connectionState = STATE_ERROR;
	}

	private void m_eob() {
		flag_reob = true;
	}

	private void m_skip(String arg) {
		if (transferringMessage != null) {
			transferringMessage = null;
			logger.l3("Message skipped : " + getString(transferringMessage));
		} else {
			logger.l4("M_SKIP while message is not sent");
		}

	}

	private void m_get(String arg) {
		if (transferringMessage != null) {
			if (messageEquals(transferringMessage, arg)) {
				int skip = Integer.valueOf(arg.split(" ")[3]);
				sendMessage(transferringMessage, skip, staticBufMaxSize);
				logger.l4("M_GET for file " + arg);
			} else {
				logger.l4("M_GET while message was not sent");
			}
		}

	}

	private void m_got(String arg) {
		if (transferringMessage != null) {
			if (messageEquals(transferringMessage, arg)) {
				logger.l3(String.format("Sent file: %s (%d)",
						transferringMessage.getMessageName(),
						transferringMessage.getMessageLength()));
				transferringMessage.delete();
				transferringMessage = null;
			} else {
				logger.l3("M_GOT for file we haven't sent: " +arg);
			}
		} else {
			logger.l4("M_GOT while message was not sent");
		}

	}

	private void m_file(String arg) {
		receivingMessage = createMessage(arg);
		long free_space = new File(FtnTools.getInbound()).getFreeSpace();
		if (receivingMessage.getMessageLength() > free_space) {
			frames.addLast(new BinkpFrame(BinkpCommand.M_SKIP,
					getString(receivingMessage)));
			receivingMessage = null;
			logger.l1("No enogth free space in inbound for receiving file");
		}
		if (!arg.split(" ")[3].equals("0")) {
			frames.addLast(new BinkpFrame(BinkpCommand.M_GET, getString(
					receivingMessage, 0)));
		} else {
			receivingBytesLeft = receivingMessage.getMessageLength();
			try {
				currentFile = File.createTempFile("temp", "jnode",
						staticTempDirectory);
				free_space = currentFile.getFreeSpace();
				if (receivingMessage.getMessageLength() > free_space) {
					logger.l1("No enogth free space in tmp for receiving file");
					currentFile.delete();
					throw new IOException();
				}
				currentOS = new FileOutputStream(currentFile);
			} catch (IOException e) {
				currentFile = null;
				if (receivingMessage.getMessageLength() < staticMemMaxSize) {
					currentOS = new ByteArrayOutputStream(
							(int) receivingMessage.getMessageLength());
				} else {
					frames.addLast(new BinkpFrame(BinkpCommand.M_SKIP,
							getString(receivingMessage)));
					receivingMessage = null;
					receivingBytesLeft = 0;
				}
			}
			if (receivingMessage != null) {
				logger.l3(String.format("Receiving file: %s (%d)",
						receivingMessage.getMessageName(),
						receivingMessage.getMessageLength()));
			}
		}

	}

	private void m_ok() {
		if (connectionState != STATE_AUTH) {
			error("We weren't waiting for M_OK");
		}
		connectionState = STATE_TRANSFER;
	}

	/**
	 * Обработка входящего M_PWD
	 * 
	 * @param arg
	 */
	private void m_pwd(String arg) {
		if (connectionState != STATE_AUTH) {
			error("We weren't waiting for M_PWD");
		}
		String password = getAuthPassword(foreignLink, secure, cramAlgo,
				cramText);

		if (password.equals(arg)) {
			String text = ((secure) ? "(S) Secure" : "(U) Unsecure")
					+ " connection";
			logger.l3(text);
			frames.addLast(new BinkpFrame(BinkpCommand.M_OK, text));
			connectionState = STATE_TRANSFER;
		} else {
			error("Invalid password");
			finish();
		}

	}

	/**
	 * Обработка входящего M_ADR
	 * 
	 * @param arg
	 */
	private void m_adr(String arg) {
		if (connectionState != STATE_ADDR) {
			error("We weren't waiting for M_ADR");
		}
		for (String addr : arg.split(" ")) {
			try {
				FtnAddress a = new FtnAddress(addr);
				Link link = FtnTools.getLinkByFtnAddress(a);
				boolean nodelist = NodelistScanner.getInstance().isExists(a) != null;
				if (link != null || nodelist) {
					foreignAddress.add(a);
				}
			} catch (NumberFormatException e) {
				logger.l4("Invalid address " + addr);
			}
		}

		if (foreignAddress.isEmpty()) {
			error("No valid address specified");
			return;
		}
		Link link = FtnTools.getLinkByFtnAddress(foreignAddress);
		if (link != null) {
			String ourAka = FtnTools.getOptionString(link,
					LinkOption.STRING_OUR_AKA);
			if (ourAka != null) {
				try {
					FtnAddress addr = new FtnAddress(ourAka);
					if (ourAddress.contains(addr)) {
						ourAddress.clear();
						ourAddress.add(addr);
					}
				} catch (NumberFormatException e) {
				}
			}
			foreignLink = link;
			secure = true;
		} else {
			boolean nodelist = false;
			for (FtnAddress a : foreignAddress) {
				if (NodelistScanner.getInstance().isExists(a) != null) {
					nodelist = true;
					break;
				}
			}
			if (!nodelist) {
				error("No one address you specified exists in Nodelist");
				return;
			}
		}
		if (clientConnection) {
			sendAddrs();
			frames.addLast(new BinkpFrame(BinkpCommand.M_PWD, getAuthPassword(
					foreignLink, secure, cramAlgo, cramText)));
		}
		connectionState = STATE_AUTH;

	}

	private void sendAddrs() {
		StringBuilder sb = new StringBuilder();
		boolean flag = true;
		for (FtnAddress a : ourAddress) {
			if (flag) {
				flag = false;
			} else {
				sb.append(" ");
			}
			sb.append(a.toString() + "@" + staticNetworkName);
		}
		frames.addLast(new BinkpFrame(BinkpCommand.M_ADR, sb.toString()));
	}

	private void m_null(String arg) {
		logger.l4("M_NULL " + arg);
		String[] args = arg.split(" ");
		if (args[0].equals("OPT")) {
			for (int i = 1; i < args.length; i++) {
				Matcher md = cramPattern.matcher(args[i]);
				if (md.matches()) {
					String[] algos = md.group(1).split("/");
					for (String algo : algos) {
						try {
							MessageDigest.getInstance(algo);
							cramText = md.group(2);
							cramAlgo = md.group(1);
							logger.l4("Remote requires MD-mode (" + algo + ")");
							break;
						} catch (NoSuchAlgorithmException e) {
							logger.l2("fail algo ", e);
							logger.l2("Remote requires MD-mode for unknown algo");
						}
					}
				}
			}
		} else if (args[0].equals("VER")) {
			if (arg.matches("^.* binkp/1\\.1$")) {
				binkp1_0 = false;
				logger.l4("Protocol version 1.1");
			} else {
				binkp1_0 = true;
				logger.l4("Protocol version 1.0");
			}
		}

	}

	private void checkForMessages() {
		if (connectionState != STATE_TRANSFER) {
			return;
		}
		if (flag_leob) {
			return;
		}

		if (transferringMessage != null) {
			return;
		}
		if (messages.isEmpty()) {
			if (foreignLink != null) {
				messages.addAll(FtnTosser.getMessagesForLink(foreignLink));
			} else {
				for (FtnAddress a : foreignAddress) {
					Link tempLink = new Link();
					tempLink.setLinkAddress(a.toString());
					messages.addAll(FtnTosser.getMessagesForLink(tempLink));
				}
			}
		}

		if (messages.isEmpty()) {
			frames.addLast(new BinkpFrame(BinkpCommand.M_EOB));
			flag_leob = true;
		} else {
			transferringMessage = messages.removeFirst();
			sendMessage(transferringMessage, 0, staticBufMaxSize);
		}

	}

	private void finish() {
		connectionState = STATE_END;
		throw new ConnectionEndException();
	}

	private void greet() {
		// check if busy
		if (ThreadPool.isBusy()) {
			frames.addLast(new BinkpFrame(BinkpCommand.M_BSY,
					"Too much connections"));
			throw new ConnectionEndException();
		}
		SystemInfo info = MainHandler.getCurrentInstance().getInfo();
		ourAddress.addAll(info.getAddressList());
		frames.addLast(new BinkpFrame(BinkpCommand.M_NUL, "SYS "
				+ info.getStationName()));
		frames.addLast(new BinkpFrame(BinkpCommand.M_NUL, "ZYZ "
				+ info.getSysop()));
		frames.addLast(new BinkpFrame(BinkpCommand.M_NUL, "LOC "
				+ info.getLocation()));
		frames.addLast(new BinkpFrame(BinkpCommand.M_NUL, "NDL "
				+ info.getNDL()));
		frames.addLast(new BinkpFrame(BinkpCommand.M_NUL, "VER "
				+ MainHandler.getVersion() + " binkp/1.1"));
		frames.addLast(new BinkpFrame(BinkpCommand.M_NUL, "TIME "
				+ format.format(new Date())));

		connectionState = STATE_ADDR;
		if (!clientConnection) {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(String.format("%d%d", System.currentTimeMillis(),
						System.nanoTime()).getBytes());
				byte[] digest = md.digest();
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < 16; i++) {
					builder.append(String.format("%02x", digest[i]));
				}
				cramText = builder.toString();
				cramAlgo = "MD5";
				frames.addLast(new BinkpFrame(BinkpCommand.M_NUL, String
						.format("OPT CRAM-MD5-%s", cramText)));
				sendAddrs();
			} catch (NoSuchAlgorithmException e) {
				cramText = null;
			}
		}
	}

	private void sendMessage(Message message, int skip, int bufMaxSize) {
		frames.addLast(new BinkpFrame(BinkpCommand.M_FILE, getString(message,
				skip)));
		logger.l3(String.format("Sending file: %s (%d)",
				message.getMessageName(), message.getMessageLength()));
		try {
			int n;
			message.getInputStream().skip(skip);
			do {
				byte[] buf = new byte[bufMaxSize];
				n = message.getInputStream().read(buf);
				sent_bytes += n;
				if (n > 0)
					frames.addLast(new BinkpFrame(buf, n));
			} while (n > 0);
		} catch (IOException e) {
		}

	}
}
