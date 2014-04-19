/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
	private static final long MILLISEC_IN_DAY = 86400000L;

	public StatPoster(Timer timer) {
		if (getStatisticEnabled()) {
			posters = new ArrayList<>();
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
						logger.l2("Poster " + poster + " started");
					} catch (Exception e) {
						logger.l1("Unable to load poster " + poster, e);
					}
				}
			}
			Calendar calendar = Calendar.getInstance(Locale.US);
			calendar.set(Calendar.DAY_OF_YEAR,
					calendar.get(Calendar.DAY_OF_YEAR));
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			Date date = new Date(calendar.getTime().getTime() + MILLISEC_IN_DAY);
			long delay = date.getTime() - new Date().getTime();
			if (delay < 0) {
				delay = 0;
			}
			logger.l3("First stat after " + (delay / 1000)
					+ " seconds and every 24h after");
			timer.schedule(this, delay, 24 * 3600 * 1000);
		}
	}

	@Override
	public void run() {
		logger.l1("StatPoster activated");
		Echoarea area = FtnTools.getAreaByName(getTechEchoarea(), null);
		for (IStatPoster poster : posters) {
			String text = poster.getText();
			if (text != null && text.length() != 0) {
				FtnTools.writeEchomail(area, poster.getSubject(), text);
				logger.l3("Posted stat from robot "
						+ poster.getClass().getCanonicalName());
			} else {
				logger.l3("Empty stat from robot "
						+ poster.getClass().getCanonicalName());
			}
		}
	}

	public String getTechEchoarea() {
		return MainHandler.getCurrentInstance().getProperty(STAT_ECHOAREA,
				"tech");
	}

	boolean getStatisticEnabled() {
		return MainHandler.getCurrentInstance().getBooleanProperty(STAT_ENABLE,
				true);
	}

}
