package jnode.stat.threads;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.stat.EchoareaStat;
import jnode.stat.FileareaStat;
import jnode.stat.IStatPoster;

public class StatPoster extends TimerTask {
	private static final IStatPoster[] posters = new IStatPoster[] {
			new EchoareaStat(), new FileareaStat() };
	private static final Logger logger = Logger.getLogger(StatPoster.class);

	public StatPoster() {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.set(Calendar.DAY_OF_YEAR,
				calendar.get(Calendar.DAY_OF_YEAR) + 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date date = calendar.getTime();
		logger.l4("First stat will run at " + date + " and every 24h after");
		new Timer().schedule(this, date, 24 * 3600 * 1000);
	}

	@Override
	public void run() {
		if (Main.isStatisticEnable()) {
			Echoarea area = FtnTools.getAreaByName(Main.getTechArea(), null);
			for (IStatPoster poster : posters) {
				FtnTools.writeEchomail(area, poster.getSubject(),
						poster.getText());
				logger.l4("Posted stat from robot "
						+ poster.getClass().getCanonicalName());
			}
		}

	}
}
