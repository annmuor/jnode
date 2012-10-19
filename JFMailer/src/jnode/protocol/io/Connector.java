package jnode.protocol.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;

import jnode.ftn.types.FtnAddress;
import jnode.jfmailer.conf.Configuration;
import jnode.jfmailer.log.Logger;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.exception.ProtocolException;

/**
 * 
 * @author kreon
 * 
 */
public class Connector {
	private Socket clientSocket;
	private ProtocolConnector connector;
	private List<Message> messages;
	private int index = 0;

	public Connector() throws ProtocolException {
		this.connector = new BinkpConnector();
		messages = new ArrayList<Message>();
	}

	private String generate8d() {
		byte[] digest = new byte[4];
		for (int i = 0; i < 4; i++) {
			long a = Math.round(Integer.MAX_VALUE * Math.random());
			long b = Math.round(Integer.MIN_VALUE * Math.random());
			long c = a ^ b;
			byte d = (byte) ((c >> 12) & 0xff);
			digest[i] = d;
		}
		return String.format("%02x%02x%02x%02x", digest[0], digest[1],
				digest[2], digest[3]);
	}

	private List<Message> getMessagesForLink(FtnAddress link) {
		ArrayList<Message> ret = new ArrayList<Message>();
		String root = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File out = new File(root + "/fido/outbound");
		out.mkdirs();
		String loreg = String.format("^%04x%04x\\..?lo$", link.getNet(),
				link.getNode());
		String netmailreg = String.format("^%04x%04x\\..?ut$", link.getNet(),
				link.getNode());
		for (File f : out.listFiles()) {
			if (f.getName().toLowerCase().matches(loreg)) {
				StringBuilder newlo = new StringBuilder();
				try {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(new FileInputStream(f)));
					String line = null;
					while ((line = br.readLine()) != null) {
						String path = line.replaceFirst("^[#^]", "");
						File file = new File(path);
						boolean split = false;
						while (!file.exists()) {
							file = new File(path.toLowerCase());
							if (!file.exists()) {
								Logger.log("File " + path + " not found");
								file = new File(path.toUpperCase());
								if (!file.exists()) {
									Logger.log("File " + path + " not found");
									if (!split) {
										split = true;
										Matcher m = Pattern
												.compile(
														".*[\\/]?([a-f0-9]{8}\\.[a-z0-9]{3})$",
														Pattern.CASE_INSENSITIVE)
												.matcher(path);
										if (m.matches()) {
											// try it ?
											String tmp = root
													+ "/fido/outbound/"
													+ m.group(1);
											Logger.log("Trying " + tmp);
											path = tmp;
										}
									} else {
										file = null;
										break;
									}
								}
							}
						}
						if (file != null) {
							try {
								Message lo = new Message(file);
								ret.add(lo);
								file.delete();
								Logger.log("Sending " + f.getAbsolutePath());
							} catch (Exception e) {
								newlo.append(path);
								newlo.append("\r\n");
							}
						} else {
							Logger.log("File " + path + " not found");
						}
					}

					br.close();
				} catch (IOException e) {
					Logger.log("Error while reading " + f.getAbsolutePath());
				}
				try {
					FileOutputStream fos = new FileOutputStream(f);
					fos.write(newlo.toString().getBytes());
					fos.close();
				} catch (IOException e) {
					Logger.log("Error while writing " + f.getAbsolutePath());
				}
			} else if (f.getName().matches(netmailreg)) {
				try {
					String filename = generate8d() + ".pkt";
					Message net = new Message(f);
					net.setMessageName(filename);
					ret.add(net);
					Logger.log("Sending " + f.getAbsolutePath() + " as "
							+ filename);
					f.delete();
				} catch (Exception e) {
					Logger.log("Error while sending " + f.getAbsolutePath());
				}
			}
		}
		return ret;
	}

	public void getMessages() {
		List<Message> messages = getMessagesForLink(Configuration.INSTANSE
				.getRemote());
		this.messages = messages;
		index = 0;
	}

	public int onReceived(final Message message) {
		String root = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File out = new File(root + "/fido/"
				+ ((message.isSecure()) ? "secure" : "insecure"));
		out.mkdirs();
		try {
			String filename = out.getAbsolutePath() + "/"
					+ message.getMessageName();
			filename = filename.toLowerCase();
			File f = new File(filename);
			boolean ninetoa = false;
			boolean ztonull = false;
			boolean underll = false;
			while (f.exists()) {
				if ((ninetoa && ztonull) || underll) {
					Logger.log("All possible files exists. Please delete something before continue");
					return 1;
				}
				char[] array = filename.toCharArray();
				char c = array[array.length - 1];
				if (c >= '0' || c <= '8' || c >= 'a' || c <= 'y') {
					c++;
				} else if (c == '9') {
					c = 'a';
					ninetoa = true;
				} else if (c == 'z') {
					c = '0';
					ztonull = true;
				} else {
					c = '_';
					underll = true;
				}
				array[array.length - 1] = c;
				filename = new String(array);
				f = new File(filename);
			}
			FileOutputStream fos = new FileOutputStream(f);
			InputStream is = message.getInputStream();
			int len = 0;
			do {
				len = is.available();
				byte[] buf;
				if (len > 1024) {
					buf = new byte[1024];
				} else {
					buf = new byte[len];
				}
				is.read(buf);
				fos.write(buf);
			} while (len > 0);
			is.close();
			fos.close();
			Logger.log("Saved " + message.getMessageName() + " to "
					+ f.getAbsolutePath());
		} catch (IOException e) {
			Logger.log("Error while saving message " + message.getMessageName());
			return 1;
		}
		return 0;

	}

	private void doSocket(Socket clientSocket) {
		InputStream is = null;
		OutputStream os = null;
		long lastactive = System.currentTimeMillis();
		try {
			is = clientSocket.getInputStream();
			os = clientSocket.getOutputStream();
		} catch (IOException e) {
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException ignore) {
				}
			}
			return;
		}

		while (!clientSocket.isClosed()) {
			try {
				if (is.available() > 0) {
					connector.avalible(is);
					lastactive = System.currentTimeMillis();
				}
			} catch (IOException ignore) {
			}

			Frame[] frames = connector.getFrames();
			if (frames != null && frames.length > 0) {
				for (Frame frame : frames) {
					try {
						os.write(frame.getBytes());
						lastactive = System.currentTimeMillis();
					} catch (IOException e) {
						try {
							if (clientSocket != null) {
								clientSocket.close();
							}
						} catch (IOException ignore) {
						}
					}
				}
			}

			if (connector.canSend()) {
				if (messages.size() > index) {
					connector.send(messages.get(index++));
				} else {
					connector.eob();
				}
				continue;
			}

			if (connector.closed()) {
				try {
					if (clientSocket != null) {
						clientSocket.close();
					}
				} catch (IOException e) {
				}
				break;
			}
			if (System.currentTimeMillis() - lastactive > 60000) {
				try {
					if (clientSocket != null) {
						clientSocket.close();
					}
				} catch (IOException ignore) {
				}
				break;
			}
		}
		messages = new ArrayList<Message>();
		index = 0;
	}

	public void connect() throws ProtocolException {
		if (Configuration.INSTANSE.getRemote() == null
				|| Configuration.INSTANSE.getLocal() == null
				|| Configuration.INSTANSE.getRemoteHost() == null) {
			throw new ProtocolException("Please, configure your mailer first");
		}

		Logger.log("Connecting to "
				+ Configuration.INSTANSE.getRemote().toString() + " ( "
				+ Configuration.INSTANSE.getRemoteHost() + ":"
				+ Configuration.INSTANSE.getRemotePort() + " )");
		connector.reset();
		connector.initOutgoing(this);
		try {
			SocketAddress soAddr = new InetSocketAddress(
					Configuration.INSTANSE.getRemoteHost(),
					Configuration.INSTANSE.getRemotePort());
			clientSocket = new Socket();
			clientSocket.connect(soAddr, 30000);
			doSocket(clientSocket);
		} catch (UnknownHostException e) {
			throw new ProtocolException("Unknown host: "
					+ Configuration.INSTANSE.getRemoteHost());
		} catch (SocketTimeoutException e) {
			throw new ProtocolException("Connection timeout");
		} catch (IOException e) {
			throw new ProtocolException(e.getLocalizedMessage());
		} finally {
			try {
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public void accept(Socket clientSocket) throws ProtocolException {
		connector.reset();
		connector.initIncoming(this);
		doSocket(clientSocket);
	}
}
