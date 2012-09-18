package jnode.main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.orm.ORMManager;

public class Client extends TimerTask {
	private static final Logger logger = Logger.getLogger(Client.class);

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
