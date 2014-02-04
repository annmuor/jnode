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

package org.jnode.httpd.routes.get;

import java.util.List;

import jnode.dto.Link;
import jnode.dto.Route;
import jnode.orm.ORMManager;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;

public class RoutingsRoute extends spark.Route {
	private static String routings = null;

	public RoutingsRoute() {
		super("/secure/route.html");
		if (routings == null) {
			routings = HTML.getContents("/parts/route.html");
		}
	}

	@Override
	public Object handle(Request req, Response resp) {
		List<Route> routes = ORMManager.get(Route.class).getOrderAnd("nice",
				true);
		StringBuilder sb = new StringBuilder();
		for (Route r : routes) {
			Link l = ORMManager.get(Link.class)
					.getById(r.getRouteVia().getId());
			sb.append(String
					.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td><a href=\"#\" class=\"css-link-1\" onclick=\"del(%d);\">Удалить</a></td></tr>",
							r.getNice(), r.getFromAddr(), r.getFromName(),
							r.getToAddr(), r.getToName(), r.getSubject(),
							(l != null) ? l.getLinkAddress() : "NULL",
							r.getId()));
		}
		StringBuilder sb2 = new StringBuilder();
		for (Link l : ORMManager.get(Link.class).getAll()) {
			if (l.getLinkAddress().matches(
					"^[1-7]:[0-9]{1,5}\\/[0-9]{1,5}(\\.0)?$")) {
				sb2.append("<option value=\"" + l.getId() + "\">"
						+ l.getLinkAddress() + "</option>");
			}
		}
		return HTML.start(true)
				.append(String.format(routings, sb.toString(), sb2.toString()))
				.footer().get();
	}

}
