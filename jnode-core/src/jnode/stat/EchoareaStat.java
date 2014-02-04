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

package jnode.stat;

import java.util.Arrays;
import java.util.Date;

import com.j256.ormlite.dao.GenericRawResults;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.NewEchoareaEvent;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;
import jnode.report.ReportBuilder;
import jnode.stat.threads.StatPoster;

public class EchoareaStat implements IStatPoster, IEventHandler {
	private StatPoster poster;

	public EchoareaStat() {
		Notifier.INSTANSE.register(NewEchoareaEvent.class, this);
	}

	@Override
	public String getSubject() {
		return "Echoarea messages per day";
	}

	@Override
	public String getText() {

		Long before = new Date().getTime() - (24 * 3600 * 1000);
		Long after = new Date().getTime();
		GenericRawResults<String[]> results = ORMManager
				.get(Echomail.class)
				.getRaw(String
						.format("SELECT a.name,a.description,count(e.id) AS count FROM echomail e"
								+ " LEFT JOIN echoarea a ON (e.echoarea_id=a.id) WHERE e.date >= %d AND e.date <= %d"
								+ " GROUP BY a.name,a.description ORDER BY count DESC",
								before, after));

		ReportBuilder builder = new ReportBuilder();
		builder.setColumns(Arrays.asList("Area", "Count", "Description"));
		builder.setColLength(Arrays.asList(35, 5, 35));

		for (String[] res : results) {

			builder.printLine(res[0], res[2], res[1]);

		}
		return builder.getText().toString();
	}

	@Override
	public void handle(IEvent event) {
		if (event instanceof NewEchoareaEvent) {
			Echoarea area = FtnTools.getAreaByName(poster.getTechEchoarea(),
					null);
			FtnTools.writeEchomail(area, "New echoarea created",
					event.getEvent());

		}

	}

	@Override
	public void init(StatPoster poster) {
		this.poster = poster;

	}
}
