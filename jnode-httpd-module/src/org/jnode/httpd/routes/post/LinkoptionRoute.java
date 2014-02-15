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

package org.jnode.httpd.routes.post;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class LinkoptionRoute extends Route {

	public LinkoptionRoute() {
		super("/secure/linkoption");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String id = req.queryParams("_id");
		if (id != null) {
			try {
				Long lid = Long.valueOf(id);
				Link link = ORMManager.get(Link.class).getById(lid);
				if (link != null) {
					for (String name : req.queryParams()) {
						if (name.startsWith("_")) {
							continue;
						}
						LinkOption option = ORMManager.get(LinkOption.class)
								.getFirstAnd("link_id", "=", link, "name", "=",
										name);
						String value = req.queryParams(name);
						if (value != null && value.length() > 0) {
							if (option == null) {
								option = new LinkOption();
								option.setLink(link);
								option.setOption(name);
							}
							option.setValue(value);
							ORMManager.get(LinkOption.class).saveOrUpdate(
									option);
						} else {
							if (option != null) {
								ORMManager.get(LinkOption.class).delete(option);
							}
						}
					}
				}
			} catch (RuntimeException e) {
			}
		}
		resp.header("Location", "/secure/links.html");
		halt(302);
		return null;
	}
}
