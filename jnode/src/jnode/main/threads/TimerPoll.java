package jnode.main.threads;

import java.sql.SQLException;
import java.util.TimerTask;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.orm.ORMManager;

public class TimerPoll extends TimerTask {
	private static final Logger logger = Logger.getLogger(TimerPoll.class);

	@Override
	public void run() {
		try {
			for (Link l : ORMManager.INSTANSE.link().queryForAll()) {
				if (FtnTools.getOptionBooleanDefTrue(l,
						LinkOption.BOOLEAN_POLL_BY_TIMEOT)) {
					PollQueue.INSTANSE.add(l);
				}
			}
		} catch (SQLException e) {
			logger.l2("Не могу получить список узлов");
		}
	}
}
