package org.jnode.httpd.routes.get;

import java.util.List;

import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;
import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class UsersRoute extends Route {
	private static String request = null;

	public UsersRoute() {
		super("/secure/users.html");
		if (request == null) {
			request = HTML.getContents("/parts/users.html");
		}
	}

	@Override
	public Object handle(Request req, Response resp) {
		List<WebAdmin> admins = ORMManager.get(WebAdmin.class).getAll();
		StringBuilder sb = new StringBuilder();
		for (WebAdmin admin : admins) {
			sb.append(String
					.format("<tr><td>%s</td><td>"
							+ "<a href=\"#\" onclick=\"changePassword(%d);\" class=\"css-link-1\">Change password</a>&nbsp;"
							+ "<a href=\"#\" onclick=\"deleteUser(%d);\" class=\"css-link-1\">Delete</a>&nbsp;",
							admin.getUsername(), admin.getId(), admin.getId()));
		}

		return HTML.start(true).append(String.format(request, sb.toString()))
				.footer().get();
	}
}
