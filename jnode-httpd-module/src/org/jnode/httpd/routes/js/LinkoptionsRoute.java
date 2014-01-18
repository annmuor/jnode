package org.jnode.httpd.routes.js;

import java.util.List;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.orm.ORMManager;

import org.jnode.httpd.routes.JsRoute;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;

public class LinkoptionsRoute extends JsRoute {

	public LinkoptionsRoute(String path) {
		super(path);
	}

	public LinkoptionsRoute(String path, String acceptType) {
		super(path, acceptType);
	}

	@Override
	public Object _handle(Request req, Response resp) {
		StringBuilder sb = new StringBuilder();
		String id = req.queryParams("id");
		if (id != null) {
			try {
				Long lid = Long.valueOf(id);
				Link link = ORMManager.get(Link.class).getById(lid);
				if (link != null) {
					List<LinkOption> options = ORMManager.get(LinkOption.class)
							.getAnd("link_id", "=", link);
					sb.append(JSONUtil.value(options));
				}
			} catch (RuntimeException e) {
			}
		}
		return sb.toString();
	}
}
