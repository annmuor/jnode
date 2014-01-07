package org.jnode.httpd.util;

import java.util.Collection;

public class JSONUtil {
	public static String pair(String var, Object value) {
		return String.format("\"%s\":%s", var, value(value));

	}

	public static String value(Object value) {
		StringBuilder sb = new StringBuilder();
		if (value instanceof Long || value instanceof Integer
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
			sb.append("\"" + value.toString().replaceAll("\"", "\\\"") + "\"");
		}
		return sb.toString();
	}
}
