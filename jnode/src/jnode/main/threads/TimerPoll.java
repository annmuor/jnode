package jnode.main.threads;

import java.sql.SQLException;
import java.util.TimerTask;

import jnode.logger.Logger;
import jnode.orm.ORMManager;

public class TimerPoll extends TimerTask {
	private static final Logger logger = Logger.getLogger(TimerPoll.class);

	@Override
	public void run() {
		try {
			PollQueue.INSTANSE.addAll(ORMManager.link().queryForAll());
		} catch (SQLException e) {
			logger.error("Не могу получить список узлов");
		}
	}
}
