package org.jnode.httpd.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jnode.dto.Link;
import jnode.dto.LinkOption;

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
				return String.format("{%s, %s, %s, %s, %s, %s, %s}",
						pair("id", object.getId()),
						pair("name", object.getLinkName()),
						pair("addr", object.getLinkAddress()),
						pair("host", object.getProtocolHost()),
						pair("port", object.getProtocolPort()),
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
