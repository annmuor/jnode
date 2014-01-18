package jnode.protocol.binkp;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.PollQueue;
import jnode.main.threads.ThreadPool;

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
			synchronized (PollQueue.getSelf()) {
				if (PollQueue.getSelf().isEmpty()) {

					try {
						PollQueue.getSelf().wait();
					} catch (InterruptedException e) {
					}
				}
				logger.l3("Proccessing poll");
				Link l = PollQueue.getSelf().getNext();
				logger.l2(String.format("Connecting to %s:%d",
						l.getProtocolHost(), l.getProtocolPort()));
				BinkpAsyncConnector conn = BinkpAsyncConnector.connect(
						l.getProtocolHost(), l.getProtocolPort());
				if (conn != null) {
					ThreadPool.execute(conn);
				}
			}
		}
	}

}
