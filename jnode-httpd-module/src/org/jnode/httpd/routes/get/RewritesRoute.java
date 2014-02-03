package org.jnode.httpd.routes.get;

import java.util.List;

import jnode.dto.Rewrite;
import jnode.orm.ORMManager;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;

public class RewritesRoute extends spark.Route {
	private static String rewrites = null;

	public RewritesRoute() {
		super("/secure/rewrite.html");
		if (rewrites == null) {
			rewrites = HTML.getContents("/parts/rewrite.html");
		}
	}

	@Override
	public Object handle(Request req, Response resp) {
		List<Rewrite> rewrites = ORMManager.get(Rewrite.class).getOrderAnd(
				"nice", true);
		StringBuilder sb = new StringBuilder();
		for (Rewrite r : rewrites) {
			sb.append(String
					.format("<tr>"
							+ "<td>%d</td>"
							+ "<td>%s/%b</td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><b>%s</b> -&gt; <b>%s</b></td>"
							+ "<td><a href=\"#\" class=\"css-link-1\" onclick=\"del(%d);\">Удалить</a></td>"
							+ "</tr>", r.getNice(), r.getType().name(),
							r.isLast(),

							r.getOrig_from_addr(), r.getNew_from_addr(),
							r.getOrig_from_name(), r.getNew_from_name(),
							r.getOrig_to_addr(), r.getNew_to_addr(),
							r.getOrig_to_name(), r.getNew_to_name(),
							r.getOrig_subject(), r.getNew_subject(), r.getId()));
		}
		return HTML.start(true)
				.append(String.format(RewritesRoute.rewrites, sb.toString()))
				.footer().get();
	}

}
