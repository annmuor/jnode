package org.jnode.httpd.routes.js;

import static org.jnode.httpd.util.JSONUtil.pair;

import org.jnode.httpd.routes.JsRoute;

import jnode.main.MainHandler;
import jnode.main.SystemInfo;
import spark.Request;
import spark.Response;

public class SelfRoute extends JsRoute {

	public SelfRoute(String path) {
		super(path);
	}

	public SelfRoute(String path, String acceptType) {
		super(path, acceptType);
	}

	@Override
	public Object _handle(Request req, Response resp) {
		SystemInfo info = MainHandler.getCurrentInstance().getInfo();
		return String.format("{%s, %s, %s, %s, %s, %s}",
				pair("name", info.getStationName()),
				pair("location", info.getLocation()),
				pair("addresses", info.getAddressList()),
				pair("sysop", info.getSysop()),
				pair("version", MainHandler.getVersion()), pair("os", getOS()));
	}

	private String getOS() {
		return System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + " ("
				+ System.getProperty("os.arch") + ")";
	}
}
