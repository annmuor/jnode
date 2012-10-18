package jnode.stat.threads;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jnode.dto.Echoarea;
import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.NewEchoareaEvent;
import jnode.event.NewFileareaEvent;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.stat.EchoareaStat;
import jnode.stat.FileareaStat;
import jnode.stat.IStatPoster;

public class StatPoster extends TimerTask implements IEventHandler {
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
		logger.l3("First stat will run at " + date + " and every 24h after");
		new Timer().schedule(this, date, 24 * 3600 * 1000);
		Notifier.INSTANSE.register(NewEchoareaEvent.class, this);
		Notifier.INSTANSE.register(NewFileareaEvent.class, this);
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

	@Override
	public void handle(IEvent event) {
		if (event instanceof NewEchoareaEvent) {
			if (Main.isStatisticEnable()) {
				Echoarea area = FtnTools
						.getAreaByName(Main.getTechArea(), null);
				FtnTools.writeEchomail(area, "New echoarea created",
						event.getEvent());
			}
		} else if (event instanceof NewFileareaEvent) {
			if (Main.isStatisticEnable()) {
				Echoarea area = FtnTools
						.getAreaByName(Main.getTechArea(), null);
				FtnTools.writeEchomail(area, "New filearea created",
						event.getEvent());
			}
		}

	}
}
