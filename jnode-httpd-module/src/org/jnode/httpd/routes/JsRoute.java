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

package org.jnode.httpd.routes;

import spark.Request;
import spark.Response;
import spark.Route;

public abstract class JsRoute extends Route {

	protected JsRoute(String path) {
		super(path);
	}

	public JsRoute(String path, String acceptType) {
		super(path, acceptType);
	}

	@Override
	public final Object handle(Request req, Response resp) {
		StringBuilder sb = new StringBuilder();
		String cb = req.queryParams("cb");
		if (cb != null) {
			sb.append(cb + "(");
		}
		sb.append(_handle(req, resp));
		if (cb != null) {
			sb.append(")");
		}
		resp.type("text/javascript");
		return sb.toString();
	}

	protected abstract Object _handle(Request req, Response resp);

}
