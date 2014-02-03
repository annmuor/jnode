package org.jnode.httpd.filters;

import spark.Filter;
import spark.Request;
import spark.Response;

public class CharsetFilter extends Filter {
	public CharsetFilter() {
		super();
	}

	@Override
	public void handle(Request request, Response response) {
		if (request.pathInfo().endsWith(".html")) {
			response.type("text/html; charset=utf-8");
		}
	}

}
