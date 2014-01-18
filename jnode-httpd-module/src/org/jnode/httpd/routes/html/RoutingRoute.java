package org.jnode.httpd.routes.html;

import jnode.dto.Link;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class RoutingRoute extends Route {

	public RoutingRoute(String path) {
		super(path);
	}

	@Override
	public Object handle(Request req, Response resp) {
		String nice = req.queryParams("nice");
		String fa = req.queryParams("fa");
		String fn = req.queryParams("fn");
		String ta = req.queryParams("ta");
		String tn = req.queryParams("tn");
		String s = req.queryParams("s");
		String v = req.queryParams("v");
		String code = null;
		String delete = req.queryParams("did");
		if (delete != null) {
			try {
				Long eid = Long.valueOf(delete);
				jnode.dto.Route del = ORMManager.get(jnode.dto.Route.class)
						.getById(eid);
				if (del != null) {
					ORMManager.get(jnode.dto.Route.class).delete(del);
				}
			} catch (RuntimeException e) {
				code = "ERROR";
			}
		} else {
			try {
				jnode.dto.Route route = new jnode.dto.Route();
				route.setNice(Long.valueOf(nice));
				route.setFromAddr(fa);
				route.setFromName(fn);
				route.setToAddr(ta);
				route.setToName(tn);
				route.setSubject(s);
				Link l = ORMManager.get(Link.class).getById(Long.valueOf(v));
				if (l != null) {
					route.setRouteVia(l);
					ORMManager.get(jnode.dto.Route.class).save(route);
				}
			} catch (RuntimeException e) {
				code = "INVALID";
			}
		}
		resp.header("Location", "/secure/route.html"
				+ ((code != null) ? "?code=" + code : ""));
		halt(302);
		return null;
	}

}
