package jnode.protocol.binkp;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.PollQueue;
import jnode.main.threads.ThreadPool;
import jnode.protocol.binkp.connector.BinkpAbstractConnector;
import jnode.protocol.binkp.connector.BinkpAsyncConnector;
import jnode.protocol.binkp.connector.BinkpPipeConnector;
import jnode.protocol.binkp.connector.BinkpSyncConnector;

public class BinkpAsyncClientPool implements Runnable {
	private static final Logger logger = Logger
			.getLogger(BinkpAsyncClientPool.class);
	private static final String BINKD_CLIENT = "binkp.client";

	@Override
	public void run() {
		if (!MainHandler.getCurrentInstance().getBooleanProperty(BINKD_CLIENT,
				true)) {
			return;
		}
		while (true) {
			Link l = null;
			synchronized (PollQueue.getSelf()) {
				if (PollQueue.getSelf().isEmpty()) {
					try {
						PollQueue.getSelf().wait();
					} catch (InterruptedException e) {
					}
				}
				l = PollQueue.getSelf().getNext();
			}
			try { // TODO: multiple providers?
				BinkpAbstractConnector conn;
				if (l.getProtocolAddress().startsWith("sync:")) {
					int port = 24554;
					String host = l.getProtocolAddress().substring(5);
					String[] data = host.split(":");
					if (data.length > 1) {
						port = Integer.valueOf(data[data.length - 1]);
						host = host.replace(":" + data[data.length - 1], "");
					}
					conn = BinkpSyncConnector.connect(host, port);
				} else if (l.getProtocolAddress().startsWith("|")) {
					String cmd = l.getProtocolAddress().substring(1);
					logger.l2("Connecting through pipe: " + cmd);
					conn = BinkpPipeConnector.connect(cmd);
				} else {
					logger.l2(String.format("Connecting to %s:%d",
							l.getProtocolHost(), l.getProtocolPort()));
					conn = BinkpAsyncConnector.connect(l.getProtocolHost(),
							l.getProtocolPort());
				}
				if (conn != null) {
					ThreadPool.execute(conn);
				}
			} catch (RuntimeException e) {
				logger.l2("Runtime exception: " + e.getLocalizedMessage());
			}
		}
	}

}
