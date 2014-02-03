package org.jnode.httpd.routes.get;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class BecomeLinkRoute extends Route {
	private static String request = null;
	private boolean enabled;

	public BecomeLinkRoute(boolean enabled) {
		super("/requestlink.html");
		this.enabled = enabled;
		if (enabled) {
			if (request == null) {
				request = HTML.getContents("/parts/requestlink.html");
			}
		}
	}

	@Override
	public Object handle(Request arg0, Response arg1) {
		if (!enabled) {
			return HTML
					.start(false)
					.append("<b>К сожалению, запрос линков отключен сисопом</b>")
					.footer().get();
		}
		return HTML.start(false).append(request).footer().get();
	}

}
