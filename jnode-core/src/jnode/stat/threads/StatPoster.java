package jnode.stat.threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.stat.IStatPoster;

public class StatPoster extends TimerTask {
	private List<IStatPoster> posters;
	private static final Logger logger = Logger.getLogger(StatPoster.class);
	private static final String STAT_ENABLE = "stat.enable";
	private static final String STAT_ECHOAREA = "stat.area";
	private static final String STAT_POSTERS = "stat.posters";

	public StatPoster() {
		if (getStatisticEnabled()) {
			posters = new ArrayList<IStatPoster>();
			{
				String[] posters = MainHandler
						.getCurrentInstance()
						.getProperty(STAT_POSTERS,
								"jnode.stat.ConnectionStat,jnode.stat.EchoareaStat,jnode.stat.FileareaStat")
						.split(",");
				for (String poster : posters) {
					try {
						Class<?> clazz = Class.forName(poster.replaceAll("\\s",
								""));
						IStatPoster instance = (IStatPoster) clazz
								.newInstance();
						instance.init(this);
						this.posters.add(instance);
					} catch (RuntimeException e) {
						logger.l1("Unable to load poster " + poster, e);
					} catch (Exception e) {
						logger.l1("Unable to load poster " + poster, e);
					}
				}
			}
			Calendar calendar = Calendar.getInstance(Locale.US);
			calendar.set(Calendar.DAY_OF_YEAR,
					calendar.get(Calendar.DAY_OF_YEAR) + 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 00);
			calendar.set(Calendar.SECOND, 0);
			Date date = calendar.getTime();
			long delay = date.getTime() - new Date().getTime();
			logger.l3("First stat after " + (delay / 1000)
					+ " seconds and every 24h after");
			new Timer().schedule(this, delay, 24 * 3600 * 1000);
		}
	}

	@Override
	public void run() {
		logger.l1("StatPoster activated");
		Echoarea area = FtnTools.getAreaByName(getTechEchoarea(), null);
		for (IStatPoster poster : posters) {
			FtnTools.writeEchomail(area, poster.getSubject(), poster.getText());
			logger.l3("Posted stat from robot "
					+ poster.getClass().getCanonicalName());
		}
	}

	public String getTechEchoarea() {
		return MainHandler.getCurrentInstance().getProperty(STAT_ECHOAREA,
				"tech");
	}

	public boolean getStatisticEnabled() {
		return MainHandler.getCurrentInstance().getBooleanProperty(STAT_ENABLE,
				true);
	}

}
