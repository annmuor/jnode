package org.jnode.httpd.routes.get;

import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class BecomePointRoute extends Route {
	private static String requestPoint = null;
	private boolean enabled;

	public BecomePointRoute(boolean enabled) {
		super("/requestpoint.html");
		this.enabled = enabled;
		if (!enabled) {
			if (requestPoint == null) {
				requestPoint = HTML.getContents("/parts/requestpoint.html");
			}
		}
	}

	@Override
	public Object handle(Request arg0, Response arg1) {
		if (!enabled) {
			return HTML
					.start(false)
					.append("<b>Sorry, automatic point requests are disabled by sysop")
					.footer().get();
		}
		StringBuilder sb = new StringBuilder();
		for (FtnAddress a : MainHandler.getCurrentInstance().getInfo()
				.getAddressList()) {
			sb.append("<option>" + a.toString() + "</option>");
		}
		return HTML.start(false)
				.append(String.format(requestPoint, sb.toString())).footer()
				.get();
	}

}
