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
		StringBuilder sb = new StringBuilder();
		SystemInfo info = MainHandler.getCurrentInstance().getInfo();
		sb.append("{");
		sb.append(pair("name", info.getStationName()));
		sb.append(", " + pair("location", info.getLocation()));
		sb.append(", " + pair("addresses", info.getAddressList()));
		sb.append(", " + pair("sysop", info.getSysop()));
		sb.append(", " + pair("running", MainHandler.getVersion()));
		sb.append("}");
		return sb.toString();
	}
}
