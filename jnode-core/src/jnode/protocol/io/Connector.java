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

import jnode.dto.Link;
import jnode.event.ConnectionEndEvent;
import jnode.event.Notifier;
import jnode.ftn.tosser.FtnTosser;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
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
	private Link link;
	private int index = 0;
	private static final Logger logger = Logger.getLogger(Connector.class);
	private FtnTosser tosser = new FtnTosser();

	public Connector(ProtocolConnector connector) throws ProtocolException {
		this.connector = connector;
		messages = new ArrayList<Message>();
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
		List<Message> messages = FtnTosser.getMessagesForLink(link);
		this.messages = messages;
		index = 0;
		logger.l4(String.format("Received %d messages for %s", messages.size(),
				link.getLinkAddress()));
	}

	public int onReceived(final Message message) {
		return tosser.tossIncoming(message, link);

	}

	private void doSocket(Socket clientSocket) {
		InputStream is = null;
		OutputStream os = null;
		boolean success = true;
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
						logger.l5("Sent frame: " + frame);
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
				logger.l3("Connection(1) timed out");
				success = false;
				try {
					if (clientSocket != null) {
						clientSocket.close();
					}
				} catch (IOException ignore) {
				}
				break;
			}
		}
		ConnectionEndEvent event;
		if (link == null) {
			event = new ConnectionEndEvent(connector.getIncoming(), success
					&& connector.getSuccess());
		} else {
			event = new ConnectionEndEvent(
					new FtnAddress(link.getLinkAddress()),
					connector.getIncoming(), success && connector.getSuccess(),
					connector.getBytesReceived(), connector.getBytesSent());
		}
		Notifier.INSTANSE.notify(event);
		messages = new ArrayList<Message>();
		index = 0;
	}

	public void connect(Link link) throws ProtocolException {
		if (link == null) {
			throw new ProtocolException("Link can not be null");
		}
		this.link = link;
		connector.reset();
		connector.initOutgoing(this);
		try {
			SocketAddress soAddr = new InetSocketAddress(
					link.getProtocolHost(), link.getProtocolPort());
			clientSocket = new Socket();
			clientSocket.connect(soAddr, 30000);
			doSocket(clientSocket);
			tosser.end();
		} catch (UnknownHostException e) {
			Notifier.INSTANSE.notify(new ConnectionEndEvent(new FtnAddress(link
					.getLinkAddress()), false, false, 0, 0));
			throw new ProtocolException("Unknown host: "
					+ link.getProtocolHost());
		} catch (SocketTimeoutException e) {
			Notifier.INSTANSE.notify(new ConnectionEndEvent(new FtnAddress(link
					.getLinkAddress()), false, false, 0, 0));
			throw new ProtocolException("Connection timeout");

		} catch (IOException e) {
			Notifier.INSTANSE.notify(new ConnectionEndEvent(new FtnAddress(link
					.getLinkAddress()), false, false, 0, 0));
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
		tosser.end();
	}
}
