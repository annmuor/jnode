package jnode.stat.threads;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.EchomailAwaiting;
import jnode.dto.Subscription;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.orm.ORMManager;
import jnode.stat.EchoareaStat;
import jnode.stat.AStatPoster;

public class StatPoster extends TimerTask {
	private static final AStatPoster[] posters = new AStatPoster[] { new EchoareaStat() };
	private static final Logger logger = Logger.getLogger(StatPoster.class);

	public StatPoster() {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.set(Calendar.DAY_OF_YEAR,
				calendar.get(Calendar.DAY_OF_YEAR) +1);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date date = calendar.getTime();
		logger.l3("First stat will run at " + date + " and every 24h after");
		new Timer().schedule(this, date, 24 * 3600 * 1000);
	}

	public static void main(String[] args) {
		new StatPoster();
	}

	@Override
	public void run() {
		if (!"0".equals(Main.getProperty(
				Main.Settings.STAT_ENABLE.getCfgline(), "0"))) {
			String areaname = Main.getProperty(
					Main.Settings.STAT_ECHOAREA.getCfgline(), "jnode.local.stat");
			Echoarea area = ORMManager.INSTANSE.getEchoareaDAO().getFirstAnd(
					"name", "=", areaname);
			if (area == null) {
				area = new Echoarea();
				area.setName(areaname);
				area.setDescription("Local statistics");
				area.setGroup("");
				area.setReadlevel(0L);
				area.setWritelevel(Long.MAX_VALUE);
				ORMManager.INSTANSE.getEchoareaDAO().save(area);
			}
			List<Subscription> subs = ORMManager.INSTANSE.getSubscriptionDAO()
					.getAnd("echoarea_id", "=", area);
			for (AStatPoster poster : posters) {
				Echomail mail = new Echomail();
				mail.setFromFTN(Main.info.getAddress().toString());
				mail.setFromName(Main.info.getStationName());
				mail.setArea(area);
				mail.setDate(new Date());
				mail.setPath("");
				mail.setSeenBy("");
				mail.setToName("All");
				mail.setSubject(poster.getSubject());
				mail.setText(poster.getText());
				ORMManager.INSTANSE.getEchomailDAO().save(mail);
				for (Subscription s : subs) {
					ORMManager.INSTANSE.getEchomailAwaitingDAO().save(
							new EchomailAwaiting(s.getLink(), mail));
				}
				logger.l3("Posted stat from robot "
						+ poster.getClass().getCanonicalName());
			}
		}

	}
}
