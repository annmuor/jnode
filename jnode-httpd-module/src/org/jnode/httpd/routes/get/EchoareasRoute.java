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

import jnode.dto.Echoarea;
import jnode.orm.ORMManager;

import org.jnode.httpd.util.HTML;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;
import spark.Route;

public class EchoareasRoute extends Route {
	private static String echoareas = null;

	public EchoareasRoute() {
		super("/secure/echoes.html");
		if (echoareas == null) {
			echoareas = HTML.getContents("/parts/echoes.html");
		}

	}

	@Override
	public Object handle(Request req, Response resp) {
		String id = req.queryParams("id");
		StringBuilder sb = new StringBuilder();
		if (id == null) {
			for (Echoarea e : ORMManager.get(Echoarea.class).getOrderAnd(
					"name", true)) {
				sb.append(String
						.format("<tr><td>%s</td><td>%s</td><td>r:%d|w:%d|g:%s</td><td><a href=\"#new\" class=\"css-link-1\" onclick=\"edit(%d);\">Изменить</a>&nbsp;<a href=\"#\" class=\"css-link-1\" onclick=\"del(%d);\">Удалить</a></td></tr>",
								e.getName(), e.getDescription(),
								e.getReadlevel(), e.getWritelevel(),
								e.getGroup(), e.getId(), e.getId()));
			}
			return HTML.start(true)
					.append(String.format(echoareas, sb.toString())).footer()
					.get();
		} else {
			try {
				String cb = req.queryParams("cb");
				if (cb != null) {
					sb.append(cb + "(");
				}
				Long eid = Long.valueOf(id);
				sb.append(JSONUtil.value(ORMManager.get(Echoarea.class)
						.getById(eid)));
				if (cb != null) {
					sb.append(")");
				}
				resp.type("text/javascript");
				return sb.toString();
			} catch (RuntimeException e) {
			}
		}
		return null;
	}
}
