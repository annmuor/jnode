package jnode.main.threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;

/**
 * 
 * @author kreon
 * 
 */
public enum PollQueue {
	INSTANSE;
	private static Logger logger = Logger.getLogger(PollQueue.class);
	private Set<Link> queue;

	private PollQueue() {
		queue = new HashSet<Link>();
	}

	public void poll() {
		if (queue.size() > 0) {
			logger.l4("PollQueue contains" + queue.size()
					+ " nodes, making poll");
			ArrayList<Link> currentQueue = new ArrayList<Link>(queue);
			queue = new HashSet<Link>();
			for (Link link : currentQueue) {
				new Poll(link).start();
			}
		}
	}

	public void add(Link link) {
		if (link.getProtocolPort() > 0) {
			queue.add(link);
		}
	}

	private static class Poll extends Thread {
		private static final Logger logger = Logger.getLogger(Poll.class);
		private Link link;

		public Poll(Link link) {
			super();
			this.link = link;
		}

		@Override
		public void run() {
			try {
				BinkpConnector binkpConnector = new BinkpConnector();
				Connector connector = new Connector(binkpConnector);
				logger.l3(String.format("Outgoing to %s (%s:%d)",
						link.getLinkAddress(), link.getProtocolHost(),
						link.getProtocolPort()));
				connector.connect(link);

			} catch (ProtocolException e) {
				logger.l2("Protocol exception", e);
			}
		}
	}
}
