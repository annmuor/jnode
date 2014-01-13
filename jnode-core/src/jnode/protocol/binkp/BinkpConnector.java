package jnode.protocol.binkp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.SystemInfo;
import jnode.ndl.FtnNdlAddress;
import jnode.ndl.NodelistScanner;
import jnode.protocol.io.Connector;
import jnode.protocol.io.Frame;
import jnode.protocol.io.Message;
import jnode.protocol.io.ProtocolConnector;

/**
 * 
 * @author kreon
 * 
 */
public class BinkpConnector implements ProtocolConnector {
	private final static String BINKP_SIZE = "binkp.size";
	private int MAX_PACKET_SIZE = MainHandler.getCurrentInstance()
			.getIntegerProperty(BINKP_SIZE, 1400);
	private static final DateFormat format = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	private static final Logger logger = Logger.getLogger(BinkpConnector.class);
	private static final int STATE_WAITADDR = 1;
	private static final int STATE_WAITOK = 2;
	private static final int STATE_WAITPWD = 4;
	private static final int STATE_TRANSFER = 8;
	private static final int STATE_END = 16;
	private static final int STATE_ERR = 32;

	private boolean reob;
	private boolean leob;
	private boolean binkp1;
	private boolean recv;
	private boolean send;
	private boolean sendfile;
	private boolean recvfile;
	private List<Frame> frames;
	private int connectionState;
	private Message currentMessage;
	private OutputStream currentOutputStream;
	private long currentMessageTimestamp;
	private int currentMessageBytesLeft;
	private File currentTempFile;
	private boolean useCram = false;
	private String cramAlgo = "";
	private String cramText;
	private String password;
	private boolean incoming = false;
	private Connector connector;
	private Link link;
	private int totalin;
	private int totalout;
	private boolean secure;
	private List<Message> sent;

	public void reset() {
		sent = new ArrayList<Message>();
		frames = new ArrayList<Frame>();
		useCram = false;
		connector = null;
		link = null;
		totalin = 0;
		totalout = 0;
		connectionState = STATE_WAITADDR;
		recvfile = false;
		sendfile = false;
		reob = false;
		leob = false;
		binkp1 = false;
		recv = false;
		send = false;
		secure = false;
	}

	private void greet() {
		frames.add(new BinkpFrame(BinkpCommand.M_NUL, "SYS "
				+ getInfo().getStationName()));
		frames.add(new BinkpFrame(BinkpCommand.M_NUL, "ZYZ "
				+ getInfo().getSysop()));
		frames.add(new BinkpFrame(BinkpCommand.M_NUL, "LOC "
				+ getInfo().getLocation()));
		frames.add(new BinkpFrame(BinkpCommand.M_NUL, "NDL "
				+ getInfo().getNDL()));
		frames.add(new BinkpFrame(BinkpCommand.M_NUL, "VER "
				+ MainHandler.getVersion() + " binkp/1.1"));
		frames.add(new BinkpFrame(BinkpCommand.M_NUL, "TIME "
				+ format.format(new Date())));
		String mAddr = null;
		if (link != null) {
			try {
				String from = FtnTools.getOption(link,
						LinkOption.STRING_OUR_AKA);
				if (MainHandler.getCurrentInstance().getInfo().getAddressList()
						.contains(new FtnAddress(from))) {
					mAddr = from + "@fidonet";
				}
			} catch (RuntimeException e) {
			}
		}
		if (mAddr == null) {
			mAddr = getOwnAddressList();
		}
		frames.add(new BinkpFrame(BinkpCommand.M_ADR, mAddr));
	}

	private String getOwnAddressList() {
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		for (FtnAddress own : getInfo().getAddressList()) {
			if (idx > 0) {
				sb.append(" ");
			}
			sb.append(own.toString());
			sb.append("@fidonet");
			idx++;
		}
		return sb.toString();
	}

	private SystemInfo getInfo() {
		return MainHandler.getCurrentInstance().getInfo();
	}

	private void error(String err) {
		logger.l3("lerror: " + err);
		frames.add(new BinkpFrame(BinkpCommand.M_ERR, err));
		connectionState = STATE_ERR;
	}

	/**
	 * При исходящем соединении
	 */
	@Override
	public void initOutgoing(Connector connector) {
		this.connector = connector;
		incoming = false;
		greet();
	}

	/**
	 * При входящем соединении - создаем диджест ( мы только в MD5 )
	 */
	@Override
	public void initIncoming(Connector connector) {
		this.connector = connector;
		incoming = true;
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
			frames.add(new BinkpFrame(BinkpCommand.M_NUL, String.format(
					"OPT CRAM-MD5-%s", cramText)));
		} catch (NoSuchAlgorithmException e) {
			cramText = null;
		}
		greet();
	}

	private BinkpCommand getCommand(int command) {
		for (BinkpCommand c : BinkpCommand.values()) {
			if (c.getCmd() == command) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Получаем фрейм из потока
	 * 
	 * @param in
	 * @return
	 */
	private BinkpFrame recv(InputStream in) {
		BinkpFrame ret = null;
		try {
			if (in.available() > 2) {
				int hw = in.read();
				int lw = in.read();
				boolean command = false;
				if ((hw >> 7) == 1) {
					command = true;
				}
				int len = (((hw << 9) | (lw << 1)) & 0xFFFF) >> 1;
				if (len > 0) {
					if (command) {
						int cmd = in.read();
						String arg = null;
						if (len > 1) {
							byte[] data = new byte[len - 1];
							in.read(data);
							if (data[data.length - 1] == 0) { // null at the end
								byte[] datawonull = new byte[data.length - 1];
								System.arraycopy(data, 0, datawonull, 0,
										data.length - 1);
								arg = new String(datawonull);
							} else {
								arg = new String(data);
							}
						}
						ret = new BinkpFrame(getCommand(cmd), arg);
					} else {
						byte[] data = new byte[len];
						int i = 0;
						while (i < len) {
							i += in.read(data, i, len - i);
						}
						ret = new BinkpFrame(data);
					}
				}
			}
		} catch (IOException e) {
			logger.l2("Frame receiving error", e);
			return null;
		}
		return ret;
	}

	@Override
	public int avalible(InputStream is) {
		Pattern cram = Pattern.compile("^CRAM-([-A-Z0-9]+)-([a-f0-9]+)$");
		BinkpFrame frame = recv(is);
		if (frame == null) {
			return 0;
		}
		logger.l5("Received frame: " + frame.toString());
		if (connectionState < STATE_TRANSFER && !frame.isCommand()) {
			error("Unknown frame");
			return 1;
		}
		if (frame.isCommand() && frame.getCommand().equals(BinkpCommand.M_ERR)) {
			error("remote told: " + new String(frame.getData()));
			return 1;
		}
		if (frame.isCommand() && frame.getCommand().equals(BinkpCommand.M_BSY)) {
			logger.l3("Remote is busy");
			connectionState = STATE_END;
			return 1;
		}
		/**
		 * M_NUL или M_ADR
		 */
		if (connectionState == STATE_WAITADDR) {
			if (frame.getCommand().equals(BinkpCommand.M_NUL)) {
				logger.l4(frame.getArg());
				String args[] = frame.getArg().split(" ");
				if (args[0].equals("OPT")) {
					for (int i = 1; i < args.length; i++) {
						Matcher md = cram.matcher(args[i]);
						if (md.matches()) {
							String[] algos = md.group(1).split("/");
							for (String algo : algos) {
								try {
									MessageDigest.getInstance(algo);
									useCram = true;
									cramText = md.group(2);
									cramAlgo = md.group(1);
									logger.l4("Remote requires MD-mode ("
											+ algo + ")");
									break;
								} catch (NoSuchAlgorithmException e) {
									logger.l2("fail algo ", e);
								}
							}
							if (!useCram) {
								logger.l4("Remote requires MD-mode for unknown algo");
							}
						}
					}
				} else if (args[0].equals("VER")) {
					if (frame.getArg().matches("^.* binkp/1\\.1$")) {
						binkp1 = true;
						logger.l4("Protocol version 1.1");
					} else {
						binkp1 = false;
						logger.l4("Protocol version 1.0");
					}
				}
			} else if (frame.getCommand().equals(BinkpCommand.M_ADR)) {
				logger.l4(frame.getArg());
				boolean authorized = false;
				if (frame.getArg() != null) {
					Pattern ftn = Pattern
							.compile(
									"([^\\S]*([1-5]:\\d{1,5}/\\d{1,5}\\.?\\d{0,5})(@fido[a-z]*)?)",
									Pattern.CASE_INSENSITIVE);
					Matcher m = ftn.matcher(frame.getArg());
					List<FtnAddress> testOk = new ArrayList<>();
					while (m.find()) {
						FtnAddress test = new FtnAddress(m.group(2));
						this.link = FtnTools.getLinkByFtnAddress(test);
						if (link != null) {
							secure = true;
							authorized = true;
							testOk.add(test);
							break;
						} else {
							FtnNdlAddress node = NodelistScanner.getInstance()
									.isExists(test);
							if (node != null) {
								testOk.add(test);
								authorized = true;
							}
						}
					}
					if (authorized) {
						if (link == null) {
							link = new Link();
							link.setLinkAddress(testOk.get(0).toString());
						}
						password = (secure) ? (link.getProtocolPassword() != null) ? link
								.getProtocolPassword() : "-"
								: "-";
						if (!incoming) {
							frames.add(new BinkpFrame(BinkpCommand.M_PWD,
									getPassword()));
						}
						connectionState = (incoming) ? STATE_WAITPWD
								: STATE_WAITOK;
					} else {
						error("unknown m_addr");
					}
				} else {
					error("Bad M_ADDR command");
				}
			}
			/**
			 * WAIK_OK
			 */
		} else if (connectionState == STATE_WAITOK) {
			if (frame.getCommand().equals(BinkpCommand.M_OK)) {
				if (!password.equals("-")) {
					logger.l4("(C) Secure session ("
							+ ((useCram) ? "cram" : "plain") + ")");
				} else {
					logger.l4("(C) Unsecure session");
				}
				connector.setLink(link);
				connectionState = STATE_TRANSFER;
			} else if (frame.getCommand().equals(BinkpCommand.M_NUL)) {
				logger.l5(frame.getArg());
			} else {
				error("Unknown frame ok");
			}
			/**
			 * Ждем пароля
			 */
		} else if (connectionState == STATE_WAITPWD) {
			if (frame.getCommand().equals(BinkpCommand.M_PWD)) {
				boolean auth = false;
				if (frame.getArg() != null) {
					if (secure) {
						String pw = frame.getArg();
						if (pw.matches("^CRAM-" + cramAlgo + "-.*")) {
							useCram = true;
							if (getPassword().equals(pw)) {
								auth = true;
							}
						} else if (pw.equals(password)) {
							auth = true;
						}
					} else {
						auth = true;
					}
				}
				if (auth) {
					frames.add(new BinkpFrame(BinkpCommand.M_OK, (password
							.equals("-")) ? "insecure" : "secure"));
					if (!password.equals("-")) {
						logger.l4("(S) Secure session ("
								+ ((useCram) ? "cram" : "plain") + ")");
					} else {
						logger.l4("(S) Unsecure session");
					}
					if (secure) {
						connector.setLink(link);
					}
					connectionState = STATE_TRANSFER;
				} else {
					error("Bad pwd");
				}
			} else if (frame.getCommand().equals(BinkpCommand.M_NUL)) {
				logger.l5(frame.getArg());
			} else {
				logger.l5("(OK) Unknown frame " + frame.toString());
			}
			/**
			 * Ждем файлов
			 */
		} else if (connectionState == STATE_TRANSFER) {
			if (frame.isCommand()) {
				String arg = frame.getArg();
				if (frame.getCommand().equals(BinkpCommand.M_FILE)) {
					Pattern p[] = new Pattern[] {
							Pattern.compile("^(\\S+) (\\d+) (\\d+) 0$"),
							Pattern.compile("^(\\S+ \\d+ \\d+) -1$") };
					Matcher m = p[1].matcher(arg);
					if (m.matches()) {
						frames.add(new BinkpFrame(BinkpCommand.M_GET, m
								.group(1) + " 0"));
						return 1;
					}
					m = p[0].matcher(arg);
					if (m.matches()) {
						currentMessage = new Message(m.group(1), new Integer(
								m.group(2)));
						currentMessageTimestamp = new Long(m.group(3));
						currentMessageBytesLeft = (int) currentMessage
								.getMessageLength();
						try {
							currentTempFile = File.createTempFile("receive",
									"jnode");
							currentOutputStream = new FileOutputStream(
									currentTempFile);
							logger.l5("Receiving to tempfile "
									+ currentTempFile.getAbsolutePath());
						} catch (IOException e) {
							currentTempFile = null;
							currentOutputStream = new ByteArrayOutputStream(
									currentMessageBytesLeft);
						}

						recvfile = true;
						logger.l3(String.format("Receiving file: %s (%d)",
								currentMessage.getMessageName(),
								currentMessage.getMessageLength()));
						return 1;
					}
				} else if (frame.getCommand().equals(BinkpCommand.M_EOB)) {
					reob = true;
				} else if (frame.getCommand().equals(BinkpCommand.M_GOT)
						&& sendfile) {
					Pattern p = Pattern.compile("^(\\S+) (\\d+) (\\d+)$");
					Matcher m = p.matcher(frame.getArg());
					if (m.matches()) {
						String messageName = m.group(1);
						int len = Integer.valueOf(m.group(2));
						logger.l3(String.format("Sent file: %s (%d)",
								messageName, len));
						totalout += len;
						sendfile = false;
						send = true;
						// delete
						for (Message mess : sent) {
							if (mess.getMessageName().equals(messageName)) {
								mess.delete();
								break;
							}
						}
					}
				} else {
					logger.l4("(TRANSFER) Unknown frame " + frame.toString());
				}
			} else {
				if (recvfile) {
					byte[] data = frame.getData();
					int len = data.length;
					try {
						if (currentMessageBytesLeft >= len) {
							currentOutputStream.write(data);
							currentMessageBytesLeft -= len;
						} else {
							currentOutputStream.write(data, 0,
									currentMessageBytesLeft);
							currentMessageBytesLeft = 0;
						}
						if (currentMessageBytesLeft == 0) {
							InputStream iz;
							currentOutputStream.close();
							if (currentTempFile != null) {
								iz = new FileInputStream(currentTempFile);
								currentTempFile.delete();
							} else {
								ByteArrayOutputStream bos = (ByteArrayOutputStream) currentOutputStream;
								iz = new ByteArrayInputStream(bos.toByteArray());
							}
							currentOutputStream = null;
							currentTempFile = null;
							currentMessage.setInputStream(iz);
							Frame m_got = new BinkpFrame(BinkpCommand.M_GOT,
									String.format("%s %d %d",
											currentMessage.getMessageName(),
											currentMessage.getMessageLength(),
											currentMessageTimestamp));
							Frame m_skip = new BinkpFrame(BinkpCommand.M_SKIP,
									String.format("%s %d %d",
											currentMessage.getMessageName(),
											currentMessage.getMessageLength(),
											currentMessageTimestamp));
							logger.l3(String.format("Received file: %s (%d)",
									currentMessage.getMessageName(),
									currentMessage.getMessageLength()));
							currentMessage.setSecure(secure);
							if (connector.onReceived(currentMessage) == 0) {
								frames.add(m_got);
							} else {
								logger.l4("Tossing failed, sending m_skip");
								frames.add(m_skip);
							}
							totalin += currentMessage.getMessageLength();
							currentMessage = null;
							recvfile = false;
							recv = true;
						}
					} catch (IOException e) {
						error("recv error " + e.getMessage());
					}
				}
			}
		}
		return 1;
	}

	@Override
	public Frame[] getFrames() {
		Frame[] frames = this.frames.toArray(new Frame[0]);
		this.frames.clear();
		return frames;
	}

	@Override
	public boolean canSend() {
		return connectionState == STATE_TRANSFER && !(leob || sendfile);
	}

	@Override
	public boolean closed() {
		if (reob && leob && connectionState == STATE_TRANSFER) {
			if ((recv || send) && binkp1) {
				leob = false;
				reob = false;
				recv = false;
				send = false;
				if (secure) {
					logger.l4("Restarting transfer");
					connector.setLink(link);
				}
			} else {
				connectionState = STATE_END;
			}
		}
		if (connectionState == STATE_ERR) {
			if (link != null) {
				logger.l3(String.format("Done with errors, Sb/Rb: %d/%d (%s)",
						totalout, totalin, link.getLinkAddress()));
			} else {
				logger.l3("Done with errors");
			}
			return true;
		} else if (connectionState == STATE_END) {
			logger.l3(String.format("Done, Sb/Rb: %d/%d (%s)", totalout,
					totalin, link.getLinkAddress()));

			return true;
		}
		return false;
	}

	@Override
	public void eob() {
		if (!sendfile) {
			leob = true;
			frames.add(new BinkpFrame(BinkpCommand.M_EOB));
		}
	}

	@Override
	public void send(Message message) {
		sent.add(message);
		sendfile = true;
		frames.add(new BinkpFrame(BinkpCommand.M_FILE, String.format(
				"%s %d %d 0", message.getMessageName(),
				message.getMessageLength(), System.currentTimeMillis() / 1000)));
		logger.l3(String.format("Sending file: %s (%d)",
				message.getMessageName(), message.getMessageLength()));
		try {
			int avalible;
			while ((avalible = message.getInputStream().available()) > 0) {
				byte[] buf;
				if (avalible > MAX_PACKET_SIZE) { // MTU 1500 ?
					buf = new byte[MAX_PACKET_SIZE];
				} else {
					buf = new byte[avalible];
				}
				message.getInputStream().read(buf);
				frames.add(new BinkpFrame(buf));
			}
		} catch (IOException e) {
			logger.l1("Send error", e);
		}
	}

	/**
	 * ff -> 255
	 * 
	 * @param s
	 * @return
	 */
	private static byte hex2decimal(String s) {
		String digits = "0123456789ABCDEF";
		s = s.toUpperCase();
		byte val = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int d = digits.indexOf(c);
			val = (byte) (16 * val + d);
		}
		return val;
	}

	/**
	 * Получаем пароль в зависимости от cram-md5
	 * 
	 * @return
	 */
	private String getPassword() {
		MessageDigest md;
		String ret;
		if (password.equals("-") || !useCram) {
			ret = password;
		} else {
			try {
				md = MessageDigest.getInstance(cramAlgo);
			} catch (NoSuchAlgorithmException e) {
				return password;
			}
			byte[] text = new byte[cramText.length() / 2];
			byte[] key = password.getBytes();
			byte[] k_ipad = new byte[64];
			byte[] k_opad = new byte[64];
			for (int i = 0; i < cramText.length(); i += 2) {
				text[i / 2] = hex2decimal(cramText.substring(i, i + 2));
			}

			for (int i = 0; i < key.length; i++) {
				k_ipad[i] = key[i];
				k_opad[i] = key[i];
			}

			for (int i = 0; i < 64; i++) {
				k_ipad[i] ^= 0x36;
				k_opad[i] ^= 0x5c;
			}
			md.update(k_ipad);
			md.update(text);
			byte[] digest = md.digest();
			md.update(k_opad);
			md.update(digest);
			digest = md.digest();
			StringBuilder builder = new StringBuilder();
			builder.append("CRAM-" + cramAlgo + "-");
			for (int i = 0; i < 16; i++) {
				builder.append(String.format("%02x", digest[i]));
			}
			ret = builder.toString();
		}
		return ret;
	}

	@Override
	public boolean getIncoming() {
		return incoming;
	}

	@Override
	public boolean getSuccess() {
		return connectionState != STATE_ERR;
	}

	@Override
	public int getBytesReceived() {
		return totalin;
	}

	@Override
	public int getBytesSent() {
		return totalout;
	}
}
