package org.jnode.httpd.util;

public interface JSONConverter<T> {
	public String convert(T object);
}
