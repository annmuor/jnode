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
import jnode.dto.LinkOption;
import jnode.orm.ORMManager;

import org.jnode.httpd.routes.JsRoute;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;

public class LinkoptionsRoute extends JsRoute {

	public LinkoptionsRoute() {
		super("/secure/linkoptions");
	}

	public LinkoptionsRoute(String path, String acceptType) {
		super(path, acceptType);
	}

	@Override
	public Object _handle(Request req, Response resp) {
		StringBuilder sb = new StringBuilder();
		String id = req.queryParams("id");
		if (id != null) {
			try {
				Long lid = Long.valueOf(id);
				Link link = ORMManager.get(Link.class).getById(lid);
				if (link != null) {
					List<LinkOption> options = ORMManager.get(LinkOption.class)
							.getAnd("link_id", "=", link);
					sb.append(JSONUtil.value(options));
				}
			} catch (RuntimeException e) {
			}
		}
		return sb.toString();
	}
}
