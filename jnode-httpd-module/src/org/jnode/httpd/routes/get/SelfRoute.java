package org.jnode.httpd.routes.get;

import jnode.main.MainHandler;
import jnode.main.SystemInfo;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class SelfRoute extends Route {
	private final String FORMAT_TABLE = "<table class=\"info\">%s</table>";
	private final String FORMAT_TR = "<tr><th>%s</th><td>%s</td></tr>";

	public SelfRoute() {
		super("/index.html");
	}

	public SelfRoute(String path) {
		super(path);
	}

	@Override
	public Object handle(Request req, Response resp) {
		SystemInfo info = MainHandler.getCurrentInstance().getInfo();
		String text = String.format(
				FORMAT_TABLE,
				String.format(FORMAT_TR, "Station Name", info.getStationName())
						+ String.format(FORMAT_TR, "Station Location",
								info.getLocation())
						+ String.format(FORMAT_TR, "Sysop's Name",
								info.getSysop())
						+ String.format(FORMAT_TR, "FTN Address(es)", info
								.getAddressList().toString())
						+ String.format(FORMAT_TR, "Version",
								MainHandler.getVersion())
						+ String.format(FORMAT_TR, "OS", getOS()));
		return HTML.start(false).append(text).footer().get();
	}

	private String getOS() {
		return System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + " ("
				+ System.getProperty("os.arch") + ")";
	}
}
