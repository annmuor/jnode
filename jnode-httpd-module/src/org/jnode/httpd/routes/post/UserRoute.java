package org.jnode.httpd.routes.post;

import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;

import spark.Request;
import spark.Response;
import spark.Route;

public class UserRoute extends Route {
	public UserRoute() {
		super("/secure/user/:action");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String action = req.params(":action");
		String code = null;
		if ("delete".equals(action)) {
			String id = req.queryParams("id");
			try {
				Long lid = Long.valueOf(id);
				WebAdmin admin = ORMManager.get(WebAdmin.class).getById(lid);
				if (admin != null)
					ORMManager.get(WebAdmin.class).delete(admin);
			} catch (RuntimeException e) {
				code = "ERROR";
				e.printStackTrace();
			}
		} else if ("password".equals(action)) {
			String id = req.queryParams("id");
			String password = req.queryParams("password");
			try {
				Long lid = Long.valueOf(id);
				WebAdmin admin = ORMManager.get(WebAdmin.class).getById(lid);
				if (admin != null) {
					admin.setPassword(FtnTools.md5(password));
					ORMManager.get(WebAdmin.class).update(admin);
				}
			} catch (RuntimeException e) {
				code = "ERROR";
				e.printStackTrace();
			}
		} else if ("create".equals(action)) {
			String username = req.queryParams("username");
			String password = req.queryParams("password");
			WebAdmin admin = new WebAdmin();
			admin.setUsername(username);
			admin.setPassword(FtnTools.md5(password));
			ORMManager.get(WebAdmin.class).save(admin);
		} else {
			code = "ERROR";
		}
		resp.header("Location", "/secure/users.html"
				+ ((code != null) ? "?code=" + code : ""));
		halt(302);
		return null;
	}
}
