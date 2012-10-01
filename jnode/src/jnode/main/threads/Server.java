package jnode.main.threads;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

import jnode.logger.Logger;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;

/**
 * 
 * @author kreon
 * 
 */
public class Server extends Thread {
	private static final Logger logger = Logger.getLogger(Server.class);

	private static class ServerClient extends Thread {
		private static final Logger logger = Logger
				.getLogger(ServerClient.class);
		private Socket socket;

		public ServerClient(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				logger.l3(String.format("Входящее соединение от %s:%d",
						socket.getInetAddress().getHostAddress(),
						socket.getPort()));
				Connector connector = new Connector(new BinkpConnector());
				connector.accept(socket);
			} catch (ProtocolException e) {
				logger.l2("Не могу инициализировать connector");
			} finally {
				try {
					socket.close();
				} catch (IOException ignore) {
				}
			}

		}

	}

	private String host;
	private int port;
	private int errors = 0;

	public Server(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	@Override
	public void run() {
		logger.l4("Сервер слушает на " + host + ":" + port);
		try {

			ServerSocket socket = new ServerSocket(port, 0,
					Inet4Address.getByName(host));
			while (!socket.isClosed() && socket.isBound()) {
				Socket clientSocket = socket.accept();
				new ServerClient(clientSocket).start();

			}
			socket.close();
		} catch (IOException e) {
			logger.l2("Ошибка сервера: " + e.getMessage());
		}
		errors++;
		if (errors < 10) {
			logger.l3("Сервер упал, перезапускаем");
			this.run();
		} else {
			logger.l2("Сервер упал 10 раз, выходим");
			System.exit(-1);
		}
	}
}
