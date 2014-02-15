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

package jnode.main.threads;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import jnode.dto.Link;
import jnode.dto.Netmail;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.logger.Logger;
import jnode.orm.ORMManager;

/**
 * Поиск лучшего роутинга для нетмейла
 * 
 * @author kreon
 * 
 */
public class NetmailFallback extends TimerTask {
	private static final Logger logger = Logger
			.getLogger(NetmailFallback.class);

	@Override
	public void run() {
		Date date = new Date(new Date().getTime() - 3600000L); // 1 hour ago
		List<Netmail> expiredNetmail = ORMManager.get(Netmail.class).getAnd(
				"last_modified", "<", date, "send", "=", false);
		if (expiredNetmail.isEmpty()) {
			return;
		}
		for (Netmail netmail : expiredNetmail) {
			
			FtnMessage msg = FtnTools.netmailToFtnMessage(netmail);
			msg.setToAddr(new FtnAddress(netmail.getToFTN()));
			Link routeVia = FtnTools.getRoutingFallback(msg,
					netmail.getRouteVia());
			if (routeVia != null) {
				netmail.setRouteVia(routeVia);
				logger.l3("Netmail #" + netmail.getId() + " re-routed via "
						+ routeVia.getLinkAddress());
			}
			netmail.setLastModified(new Date());
			ORMManager.get(Netmail.class).update(netmail);
		}
	}

}
