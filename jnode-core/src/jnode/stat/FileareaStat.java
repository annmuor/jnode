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

import java.util.Date;

import jnode.dto.Echoarea;
import jnode.dto.Filemail;
import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.NewFileareaEvent;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;
import jnode.stat.threads.StatPoster;

public class FileareaStat implements IStatPoster, IEventHandler {
	private StatPoster poster;

	public FileareaStat() {
		Notifier.INSTANSE.register(NewFileareaEvent.class, this);
	}

	@Override
	public String getSubject() {
		return "New files last 24h";
	}

	@Override
	public String getText() {
		StringBuilder b = new StringBuilder();
		String filearea = "";
		Date d = new Date(new Date().getTime() - (24 * 3600 * 1000));
		for (Filemail m : ORMManager.get(Filemail.class).getOrderAnd(
				"filearea_id", true, "created", ">", d)) {
			if (!filearea.equalsIgnoreCase(m.getFilearea().getName())) {
				filearea = m.getFilearea().getName().toUpperCase();
				b.append(" > " + filearea + " - "
						+ m.getFilearea().getDescription() + "\n");
			}
			b.append("  " + m.getFilename() + " - " + m.getFiledesc() + "\n");
		}
		return b.toString();
	}

	@Override
	public void handle(IEvent event) {
		if (event instanceof NewFileareaEvent) {
			Echoarea area = FtnTools.getAreaByName(poster.getTechEchoarea(),
					null);
			FtnTools.writeEchomail(area, "New filearea created",
					event.getEvent());
		}

	}

	@Override
	public void init(StatPoster poster) {
		this.poster = poster;

	}

}
