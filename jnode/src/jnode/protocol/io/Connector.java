package jnode.protocol.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import jnode.dto.Link;
import jnode.ftn.tosser.FtnTosser;
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

	private class TossThread extends Thread {
		private Message message;
		private Link _link;

		private TossThread(Message message, Link link) {
			this.message = message;
			this._link = link;
		}

		@Override
		public void run() {
			FtnTosser.tossIncoming(message, _link);
		}
	}

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
		logger.debug(String.format("Получаем сообщения для %s ",
				link.getLinkAddress()));
		List<Message> messages = FtnTosser.getMessagesForLink(link);
		this.messages = messages;
		index = 0;
	}

	public void onReceived(Message message) {
		TossThread t = new TossThread(message, link);
		t.start();
		t = null;
	}

	private void doSocket(Socket clientSocket) {
		InputStream is = null;
		OutputStream os = null;
		long lastactive = System.currentTimeMillis();
		try {
			clientSocket.setSoTimeout(30000);
			is = clientSocket.getInputStream();
			os = clientSocket.getOutputStream();
		} catch (IOException e) {
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
							clientSocket.close();
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
					clientSocket.close();
				} catch (IOException e) {
				}
				break;
			}
			if (System.currentTimeMillis() - lastactive > 30000) {
				logger.info("Соединение разорвано по таймауту");
				try {
					clientSocket.close();
				} catch (IOException ignore) {
				}
				break;
			}
		}
		messages = new ArrayList<Message>();
		index = 0;
	}

	public void connect(Link link) throws ProtocolException {
		if (link == null) {
			throw new ProtocolException("Для connect() надо указать линк");
		}
		connector.reset();
		connector.initOutgoing(this);
		try {
			clientSocket = new Socket(link.getProtocolHost(),
					link.getProtocolPort());
			doSocket(clientSocket);
		} catch (UnknownHostException e) {
			throw new ProtocolException("Неизвестный хост:"
					+ link.getProtocolHost());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProtocolException(e.getLocalizedMessage());
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void accept(Socket clientSocket) throws ProtocolException {
		connector.reset();
		connector.initIncoming(this);
		try {
			doSocket(clientSocket);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
