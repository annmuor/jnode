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

import jnode.dto.Rewrite;
import jnode.orm.ORMManager;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;

public class RewritesRoute extends spark.Route {
	private static String rewrites = null;

	public RewritesRoute() {
		super("/secure/rewrite.html");
		if (rewrites == null) {
			rewrites = HTML.getContents("/parts/rewrite.html");
		}
	}

	@Override
	public Object handle(Request req, Response resp) {
		List<Rewrite> rewrites = ORMManager.get(Rewrite.class).getOrderAnd(
				"nice", true);
		StringBuilder sb = new StringBuilder();
		for (Rewrite r : rewrites) {
			sb.append(String
					.format("<tr>"
							+ "<td>%d</td>"
							+ "<td>%s/%b</td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><a href=\"#\" class=\"css-link-1\" onclick=\"del(%d);\">Удалить</a></td>"
							+ "</tr>", r.getNice(), r.getType().name(),
							r.isLast(),

							r.getOrig_from_addr(), r.getNew_from_addr(),
							r.getOrig_from_name(), r.getNew_from_name(),
							r.getOrig_to_addr(), r.getNew_to_addr(),
							r.getOrig_to_name(), r.getNew_to_name(),
							r.getOrig_subject(), r.getNew_subject(), r.getId()));
		}
		return HTML.start(true)
				.append(String.format(RewritesRoute.rewrites, sb.toString()))
				.footer().get();
	}

}
