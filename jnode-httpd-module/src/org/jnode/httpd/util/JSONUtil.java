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

package org.jnode.httpd.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jnode.dto.Echoarea;
import jnode.dto.Filearea;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Route;

public class JSONUtil {
	private static final Map<Class<?>, JSONConverter<?>> converterMap = createConverterMap();;

	public static String pair(String var, Object value) {
		return String.format("\"%s\":%s", var, value(value));

	}

	private static Map<Class<?>, JSONConverter<?>> createConverterMap() {
		Map<Class<?>, JSONConverter<?>> map = new HashMap<>();
		map.put(Link.class, new JSONConverter<Link>() {

			@Override
			public String convert(Link object) {
				return String.format("{%s, %s, %s, %s, %s, %s}",
						pair("id", object.getId()),
						pair("name", object.getLinkName()),
						pair("addr", object.getLinkAddress()),
						pair("address", object.getProtocolAddress()),
						pair("password", object.getProtocolPassword()),
						pair("pktpassword", object.getPaketPassword()));
			}
		});
		map.put(LinkOption.class, new JSONConverter<LinkOption>() {

			@Override
			public String convert(LinkOption object) {
				return String.format("{%s, %s}",
						pair("name", object.getOption()),
						pair("value", object.getValue()));

			}
		});

		map.put(Echoarea.class, new JSONConverter<Echoarea>() {

			@Override
			public String convert(Echoarea object) {
				return String.format("{%s,%s,%s,%s,%s,%s}",
						pair("id", object.getId()),
						pair("name", object.getName()),
						pair("descr", object.getDescription()),
						pair("rl", object.getReadlevel()),
						pair("wl", object.getWritelevel()),
						pair("gr", object.getGroup()));

			}
		});

		map.put(Filearea.class, new JSONConverter<Filearea>() {

			@Override
			public String convert(Filearea object) {
				return String.format("{%s,%s,%s,%s,%s,%s}",
						pair("id", object.getId()),
						pair("name", object.getName()),
						pair("descr", object.getDescription()),
						pair("rl", object.getReadlevel()),
						pair("wl", object.getWritelevel()),
						pair("gr", object.getGroup()));

			}
		});

		map.put(Route.class, new JSONConverter<Route>() {

			@Override
			public String convert(Route object) {
				return String.format("{%s,%s,%s,%s,%s,%s,%s,%s}",
						pair("id", object.getId()),
						pair("nice", object.getNice()),
						pair("fa", object.getFromAddr()),
						pair("fn", object.getFromName()),
						pair("ta", object.getToAddr()),
						pair("tn", object.getToName()),
						pair("s", object.getSubject()),
						pair("v", object.getRouteVia()));

			}
		});
		return map;
	}

	@SuppressWarnings("unchecked")
	public static <T> String value(T value) {
		StringBuilder sb = new StringBuilder();
		if (value == null) {
			sb.append("{}");
		} else if (value instanceof Long || value instanceof Integer
				|| value instanceof Short || value instanceof Float
				|| value instanceof Double || value instanceof Boolean) {
			sb.append(value.toString());
		} else if (value instanceof Collection<?>) {
			sb.append("[");
			boolean f = true;
			for (Object t : (Collection<?>) value) {
				if (f) {
					f = false;
				} else {
					sb.append(",");
				}
				sb.append(value(t));
			}
			sb.append("]");
		} else {
			JSONConverter<T> conv = (JSONConverter<T>) converterMap.get(value
					.getClass());
			if (conv != null) {
				sb.append(conv.convert(value));
			} else {
				sb.append("\"" + value.toString().replaceAll("\"", "\\\"")
						+ "\"");
			}
		}
		return sb.toString();
	}
}
