package org.jnode.httpd.routes.get;

import org.jnode.httpd.routes.JsRoute;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;

public class HealthRoute extends JsRoute {

	public HealthRoute(String path) {
		super(path);
	}

	public HealthRoute(String path, String acceptType) {
		super(path, acceptType);
	}

	@Override
	protected Object _handle(Request req, Response resp) {
		Runtime runtime = Runtime.getRuntime();

		return String.format(
				"{%s, %s, %s, %s, %s}",
				JSONUtil.pair("threads", Thread.activeCount()),
				JSONUtil.pair("freemem",
						Math.round(runtime.freeMemory() / (1024 * 1024))),
				JSONUtil.pair("totalmem",
						Math.round(runtime.totalMemory() / (1024 * 1024))),
				JSONUtil.pair("maxmem",
						Math.round(runtime.maxMemory() / (1024 * 1024))),
				JSONUtil.pair("cores", runtime.availableProcessors()));
	}
}
