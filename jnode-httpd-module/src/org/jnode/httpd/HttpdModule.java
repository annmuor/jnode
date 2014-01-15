package org.jnode.httpd;

import jnode.event.IEvent;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;

import org.jnode.httpd.filters.SecureFilter;
import org.jnode.httpd.routes.html.LinkOptionRoute;
import org.jnode.httpd.routes.html.LinkRoute;
import org.jnode.httpd.routes.js.LinkOptionsRoute;
import org.jnode.httpd.routes.js.LinksRoute;
import org.jnode.httpd.routes.js.SelfRoute;

/**
 * HttpdModule - модуль, слушающий порт и отдающий странички
 * 
 * @author kreon
 * 
 */
import spark.Spark;

public class HttpdModule extends JnodeModule {
	private static final String CONFIG_PORT = "port";
	private Short port;

	public HttpdModule(String configFile) throws JnodeModuleException {
		super(configFile);
		port = Short.valueOf(properties.getProperty(CONFIG_PORT, "8080"));
	}

	@Override
	public void handle(IEvent event) {

	}

	@Override
	public void start() {
		Spark.setPort(port);
		Spark.staticFileLocation("/www");

		Spark.before(new SecureFilter("/secure/*"));

		Spark.get(new SelfRoute("/self"));

		Spark.get(new LinksRoute("/secure/links"));

		Spark.post(new LinkRoute("/secure/link"));

		Spark.get(new LinkOptionsRoute("/secure/linkoptions"));

		Spark.post(new LinkOptionRoute("/secure/linkoption"));
	}
}
