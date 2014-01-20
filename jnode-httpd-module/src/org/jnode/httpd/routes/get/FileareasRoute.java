package org.jnode.httpd.routes.get;

import jnode.dto.Filearea;
import jnode.orm.ORMManager;

import org.jnode.httpd.util.HTML;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;
import spark.Route;

public class FileareasRoute extends Route {
	private static String echoareas = null;

	public FileareasRoute() {
		super("/secure/fechoes.html");
		if (echoareas == null) {
			echoareas = HTML.getContents("/parts/fechoes.html");
		}
	}

	@Override
	public Object handle(Request req, Response resp) {
		String id = req.queryParams("id");
		StringBuilder sb = new StringBuilder();
		if (id == null) {
			for (Filearea e : ORMManager.get(Filearea.class).getOrderAnd(
					"name", true)) {
				sb.append(String
						.format("<tr><td>%s</td><td>%s</td><td>r:%d|w:%d|g:%s</td><td><a href=\"#new\" class=\"css-link-1\" onclick=\"edit(%d);\">Edit</a>&nbsp;<a href=\"#\" class=\"css-link-1\" onclick=\"del(%d);\">Delete</a></td></tr>",
								e.getName(), e.getDescription(),
								e.getReadlevel(), e.getWritelevel(),
								e.getGroup(), e.getId(), e.getId()));
			}
			return HTML.start(true)
					.append(String.format(echoareas, sb.toString())).footer()
					.get();
		} else {
			try {
				String cb = req.queryParams("cb");
				if (cb != null) {
					sb.append(cb + "(");
				}
				Long eid = Long.valueOf(id);
				sb.append(JSONUtil.value(ORMManager.get(Filearea.class)
						.getById(eid)));
				if (cb != null) {
					sb.append(")");
				}
				resp.type("text/javascript");
				return sb.toString();
			} catch (RuntimeException e) {
			}
		}
		return null;
	}

}
