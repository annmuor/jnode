package jnode.protocol.binkp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.ThreadPool;

import com.j256.ormlite.logger.LocalLog;

public class BinkpAsyncServer implements Runnable {
	private static final Logger logger = Logger
			.getLogger(BinkpAsyncServer.class);

	private static final String BINKD_BIND = "binkp.bind";
	private static final String BINKD_PORT = "binkp.port";
	private static final String BINKD_SERVER = "binkp.server";

	@Override
	public void run() {
		if (!MainHandler.getCurrentInstance().getBooleanProperty(BINKD_SERVER,
				true)) {
			return;
		}
		try {
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(
					new InetSocketAddress(MainHandler.getCurrentInstance()
							.getProperty(BINKD_BIND, "0.0.0.0"), MainHandler
							.getCurrentInstance().getIntegerProperty(
									BINKD_PORT, 24554)), 5);
			Selector selector = Selector.open();
			server.register(selector, server.validOps());
			while (true) {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					ServerSocketChannel channel = (ServerSocketChannel) key
							.channel();
					if (key.isValid()) {
						if (key.isAcceptable()) {
							SocketChannel client = channel.accept();
							InetSocketAddress addr = (InetSocketAddress) client
									.getRemoteAddress();
							logger.l2(String.format(
									"Incoming connection from %s:%d",
									addr.getHostString(), addr.getPort()));
							ThreadPool.execute(BinkpAsyncConnector
									.accept(client));
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
		new MainHandler("/home/kreon/jnode-test/test.conf");
		new ThreadPool(10);
		ThreadPool.execute(new BinkpAsyncServer());
	}

}
