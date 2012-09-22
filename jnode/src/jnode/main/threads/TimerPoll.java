package jnode.main.threads;

import java.sql.SQLException;
import java.util.List;
import java.util.TimerTask;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.orm.ORMManager;

public class TimerPoll extends TimerTask {
	private static final Logger logger = Logger.getLogger(TimerPoll.class);

	@Override
	public void run() {
		try {
			List<Link> links = ORMManager.link().queryForAll();
			for (Link l : links) {
				PollQueue.INSTANSE.add(l);
			}
		} catch (SQLException e) {
			logger.error("Не могу получить список узлов:"
					+ e.getLocalizedMessage());
		}
	}
}
