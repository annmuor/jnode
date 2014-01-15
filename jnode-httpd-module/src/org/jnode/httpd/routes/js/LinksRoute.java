package org.jnode.httpd.routes.js;

import java.util.List;

import jnode.dto.Link;
import jnode.orm.ORMManager;

import org.jnode.httpd.routes.JsRoute;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;

public class LinksRoute extends JsRoute {

	public LinksRoute(String path) {
		super(path);
	}

	public LinksRoute(String path, String acceptType) {
		super(path, acceptType);
	}

	@Override
	public Object _handle(Request req, Response resp) {
		StringBuilder sb = new StringBuilder();
		String id = req.queryParams("id");
		if (id != null) {
			try {
				Long lid = Long.valueOf(id);
				Link l = ORMManager.get(Link.class).getById(lid);
				sb.append(JSONUtil.value(l));
			} catch (NumberFormatException e) {

			}
		} else {
			List<Link> links = ORMManager.get(Link.class).getOrderAnd(
					"ftn_address", false);
			sb.append(JSONUtil.value(links));
		}
		return sb.toString();
	}

}
