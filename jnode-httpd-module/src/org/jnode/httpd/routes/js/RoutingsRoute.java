package org.jnode.httpd.routes.js;

import java.util.List;

import jnode.dto.Link;
import jnode.dto.Route;
import jnode.orm.ORMManager;

import org.jnode.httpd.routes.JsRoute;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;

public class RoutingsRoute extends JsRoute {

	public RoutingsRoute(String path) {
		super(path);
	}

	@Override
	protected Object _handle(Request req, Response resp) {
		resp.type("text/javascript");
		List<Route> routes = ORMManager.get(Route.class).getOrderAnd("nice",
				true);
		for (Route r : routes) {
			r.setRouteVia(ORMManager.get(Link.class).getById(
					r.getRouteVia().getId()));
		}
		return JSONUtil.value(routes);
	}

}
