package jnode.main.threads;

import java.util.TimerTask;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;

public class TimerPoll extends TimerTask {

	@Override
	public void run() {
		for (Link l : ORMManager.get(Link.class).getAnd("host", "ne", "-",
				"port", "ne", 0)) {
			if (FtnTools.getOptionBooleanDefTrue(l,
					LinkOption.BOOLEAN_POLL_BY_TIMEOT)) {
				PollQueue.getSelf().add(l);
			}
		}
	}
}
