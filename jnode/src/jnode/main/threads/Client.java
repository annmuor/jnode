package jnode.main.threads;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.orm.ORMManager;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;

public class Client extends TimerTask {
	private static final Logger logger = Logger.getLogger(Client.class);

	public static class Poll extends Thread {
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
				logger.info(String.format("Соединяемся с %s (%s:%d)",
						link.getLinkAddress(), link.getProtocolHost(),
						link.getProtocolPort()));
				connector.connect(link);

			} catch (ProtocolException e) {
				logger.error("Ошибка протокола:" + e.getMessage());
			}
		}
	}

	@Override
	public void run() {
		try {
			List<Link> links = ORMManager.link().queryForAll();
			List<Thread> thread = new ArrayList<Thread>();
			for (Link l : links) {
				if (!"".equals(l.getProtocolHost()) && l.getProtocolPort() > 0) {
					thread.add(new Poll(l));
				}
			}
			for (Thread t : thread) {
				t.start();
				t = null;
			}
		} catch (SQLException e) {
			logger.error("Не могу получить список узлов:"
					+ e.getLocalizedMessage());
		}
	}
}
