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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Переводим .html :-)
 * 
 * @author kreon
 * 
 */
@Deprecated
public class MainRoute extends Route {
	private static final Pattern include = Pattern.compile("<\\?[ ]+include=[ ]*([^ ]*)[ ]+\\?>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	public MainRoute() {
		super("*");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String path = req.pathInfo();
		if (path == null || path.length() == 0) {
			resp.header("Location", "/index.html");
			halt(302);
		} else {
			String content = getContents(path);
			if (content != null) {
				return content;
			} else {
				halt(404, "Not Found");
			}
		}
		return null;
	}

	private String getContents(String path) {
		String search = "www" + path;
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(search);
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			try {
				int n = 0;
				do {
					byte[] buf = new byte[1024];
					n = is.read(buf);
					if (n > 0) {
						sb.append(new String(buf, 0, n));
					}
				} while (n > 0);
				String content = sb.toString();
				Matcher m = include.matcher(content);
				while (m.find()) {
					String incContent = getContents(m.group(1));
					if (incContent != null) {
						content = content.replace(m.group(), incContent);
					}
				}
				return content;
			} catch (IOException e) {
			}
		} else {
		}
		return null;
	}
}
