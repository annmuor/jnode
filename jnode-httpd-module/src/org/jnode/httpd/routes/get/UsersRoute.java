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

import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;
import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class UsersRoute extends Route {
	private static String request = null;

	public UsersRoute() {
		super("/secure/users.html");
		if (request == null) {
			request = HTML.getContents("/parts/users.html");
		}
	}

	@Override
	public Object handle(Request req, Response resp) {
		List<WebAdmin> admins = ORMManager.get(WebAdmin.class).getAll();
		StringBuilder sb = new StringBuilder();
		for (WebAdmin admin : admins) {
			sb.append(String
					.format("<tr><td>%s</td><td>"
							+ "<a href=\"#\" onclick=\"changePassword(%d);\" class=\"css-link-1\">Пароль</a>&nbsp;"
							+ "<a href=\"#\" onclick=\"deleteUser(%d);\" class=\"css-link-1\">Удалить</a>&nbsp;",
							admin.getUsername(), admin.getId(), admin.getId()));
		}

		return HTML.start(true).append(String.format(request, sb.toString()))
				.footer().get();
	}
}
