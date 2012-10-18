package jnode.protocol.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import jnode.ftn.types.FtnAddress;
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
	private FtnAddress link;

	public FtnAddress getLink() {
		return link;
	}

	public Connector(ProtocolConnector connector) throws ProtocolException {
		this.connector = connector;
		messages = new ArrayList<Message>();
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	private List<Message> getMessagesForLink(FtnAddress link) {
		//
		return new ArrayList<Message>();
	}

	public void setLink(FtnAddress link) {
		this.link = link;
		List<Message> messages = getMessagesForLink(link);
		this.messages = messages;
		index = 0;
	}

	public int onReceived(final Message message) {
		return 1;

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

	public void connect(FtnAddress link, String host, int port)
			throws ProtocolException {
		this.link = link;
		connector.reset();
		connector.initOutgoing(this);
		try {
			SocketAddress soAddr = new InetSocketAddress(host, port);
			clientSocket = new Socket();
			clientSocket.connect(soAddr, 30000);
			doSocket(clientSocket);
		} catch (UnknownHostException e) {
			throw new ProtocolException("Unknown host: " + host);
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
