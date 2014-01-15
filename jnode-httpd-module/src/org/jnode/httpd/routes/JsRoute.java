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
