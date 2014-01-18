package org.jnode.httpd.routes.js;

import jnode.dto.Filearea;
import jnode.orm.ORMManager;

import org.jnode.httpd.routes.JsRoute;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;

public class FileareasRoute extends JsRoute {

	public FileareasRoute(String path) {
		super(path);
	}

	@Override
	protected Object _handle(Request req, Response resp) {
		resp.type("text/javascript");
		String id = req.queryParams("id");
		if (id == null) {
			return JSONUtil.value(ORMManager.get(Filearea.class).getOrderAnd(
					"name", true));
		} else {
			try {
				Long eid = Long.valueOf(id);
				return JSONUtil.value(ORMManager.get(Filearea.class).getById(
						eid));
			} catch (RuntimeException e) {
			}
		}
		return null;
	}

}
