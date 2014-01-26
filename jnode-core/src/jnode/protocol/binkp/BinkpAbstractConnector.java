package jnode.protocol.binkp;

import static jnode.protocol.binkp.BinkpProtocolTools.createMessage;
import static jnode.protocol.binkp.BinkpProtocolTools.forwardToTossing;
import static jnode.protocol.binkp.BinkpProtocolTools.getAuthPassword;
import static jnode.protocol.binkp.BinkpProtocolTools.getString;
import static jnode.protocol.binkp.BinkpProtocolTools.messageEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Selector;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.core.ConcurrentDateFormatAccess;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.ftn.FtnTools;
import jnode.ftn.tosser.FtnTosser;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.SystemInfo;
import jnode.main.threads.PollQueue;
import jnode.main.threads.ThreadPool;
import jnode.ndl.NodelistScanner;
import jnode.protocol.io.Message;

/**
 * Абстрактный binkp через любой протокол
 * 
 * @author kreon
 * 
 */
public abstract class BinkpAbstractConnector implements Runnable {
	static final Logger logger = Logger.getLogger(BinkpAbstractConnector.class);

	private static final ConcurrentDateFormatAccess format = new ConcurrentDateFormatAccess(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	private static final Pattern cramPattern = Pattern
			.compile("^CRAM-([-A-Z0-9]+)-([a-f0-9]+)$");
	private static final int STATE_GREET = 0;
	protected static final int STATE_ERROR = 1;
	protected static final int STATE_END = 2;
	private static final int STATE_ADDR = 3;
	private static final int STATE_AUTH = 4;
	private static final int STATE_TRANSFER = 5;
	private static final String BINKP_NETWORK_NAME = "binkp.network";
	private static final String BINKP_MAX_MEM = "binkp.maxmem";
	private static final String BINKP_TEMP = "bink.temp";
	private static final String BINKP_SIZE = "binkp.size";
	private static final String BINKP_TIMEOUT = "binkp.timeout";
	protected static Integer staticMemMaxSize = null;
	protected static Integer staticBufMaxSize = null;
	protected static File staticTempDirectory = null;
	protected static String staticNetworkName = null;
	protected static Long staticMaxTimeout = null;

	protected static void init() {
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

	protected int connectionState = STATE_GREET;
	protected List<FtnAddress> foreignAddress = new ArrayList<>();
	private List<FtnAddress> ourAddress = new ArrayList<>();
	protected Link foreignLink;
	private boolean secure = false;
	protected boolean clientConnection = true;
	private String cramAlgo = null;
	private String cramText = null;
	protected boolean binkp1_0 = true;
	private LinkedList<Message> messages = new LinkedList<>();
	protected Message transferringMessage = null;
	private Message receivingMessage = null;
	private File currentFile;
	protected OutputStream currentOS;
	private long receivingBytesLeft;
	protected boolean flag_leob = false;
	protected boolean flag_reob = false;
	protected int sent_bytes = 0;
	protected int recv_bytes = 0;
	protected int total_sent_bytes = 0;
	protected int total_recv_bytes = 0;
	protected Selector selector;
	protected LinkedList<BinkpFrame> frames = new LinkedList<>();

	public abstract void run();

	public BinkpAbstractConnector() {
		super();
	}

	protected void error(String text) {
		frames.clear();
		frames.addLast(new BinkpFrame(BinkpCommand.M_ERR, text));
		logger.l2("Local error: " + text);
		connectionState = STATE_ERROR;
	}

	protected void proccessFrame(BinkpFrame frame) {
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
				m_ok(frame.getArg());
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
				logger.l3("M_GOT for file we haven't sent: " + arg);
			}
		} else {
			logger.l4("M_GOT while message was not sent");
		}

	}

	private void m_file(String arg) {
		receivingMessage = createMessage(arg, secure);
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

	private void m_ok(String arg) {
		if (connectionState != STATE_AUTH) {
			error("We weren't waiting for M_OK");
		}
		String text = ((secure) ? "(S) Secure" : "(U) Unsecure")
				+ " connection with "
				+ ((secure) ? foreignLink.getLinkAddress() : foreignAddress
						.get(0)) + " remote said: " + arg;
		logger.l3(text);
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
		boolean valid = (!secure || checkPassword(arg));
		String text;
		if (secure) {
			text = "(S) Secure  connection with "
					+ foreignLink.getLinkAddress();
		} else {
			text = "(U) Unsecure connection with " + foreignAddress.get(0);
		}
		if (valid) {
			logger.l3(text);
			frames.addLast(new BinkpFrame(BinkpCommand.M_OK, text));
			connectionState = STATE_TRANSFER;
		} else {
			error("Invalid password");
			connectionState = STATE_ERROR;
		}
	}

	private boolean checkPassword(String arg) {
		String password = foreignLink.getProtocolPassword();
		if (password.equals(arg)) {
			return true;
		}
		password = getAuthPassword(foreignLink, secure, cramAlgo, cramText);
		if (password.endsWith(arg)) {
			return true;
		}
		return false;
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

		for (FtnAddress addr : foreignAddress) {
			if (PollQueue.getSelf().isActive(addr)) {
				busy("Already connected");
				return;
			}
		}

		for (FtnAddress addr : foreignAddress) {
			PollQueue.getSelf().start(addr);
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
			frames.addLast(new BinkpFrame(BinkpCommand.M_PWD, getAuthPassword(
					foreignLink, secure, cramAlgo, cramText)));
		} else {
			sendAddrs();
		}
		connectionState = STATE_AUTH;

	}

	private void busy(String string) {
		frames.clear();
		frames.addLast(new BinkpFrame(BinkpCommand.M_BSY, string));
		connectionState = STATE_END;
		logger.l3("Local busy: " + string);
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

	protected void checkForMessages() {
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
			} else if (clientConnection) { // unsecure only whel we make poll
				for (FtnAddress a : foreignAddress) {
					Message m = FtnTosser.getDirectUnsecureMail(a);
					if (m != null) {
						messages.add(m);
					}
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

	protected void finish() {
		throw new ConnectionEndException();
	}

	protected void greet() {
		// check if busy
		if (ThreadPool.isBusy()) {
			frames.addLast(new BinkpFrame(BinkpCommand.M_BSY,
					"Too much connections"));
			finish();
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
		if (clientConnection) {
			sendAddrs();
		} else {
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
			error("IOException");
		}

	}

	protected void checkEOB() {
		if (flag_leob && flag_reob) {
			total_recv_bytes += recv_bytes;
			total_sent_bytes += sent_bytes;
			if (sent_bytes + recv_bytes == 0 || binkp1_0) {
				connectionState = STATE_END;
				finish();
			} else {
				flag_leob = false;
				flag_reob = false;
				sent_bytes = 0;
				recv_bytes = 0;
			}
		}
	}
	
	protected void end() {
		for (FtnAddress addr : foreignAddress) {
			PollQueue.getSelf().start(addr);
		}
	}

}