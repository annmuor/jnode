package org.jnode.httpd;

import jnode.event.IEvent;
import jnode.ftn.FtnTools;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;
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

		try {
			WebAdmin admin = ORMManager.get(WebAdmin.class).getFirstAnd();
			if (admin == null) {
				admin = new WebAdmin();
				admin.setUsername("admin");
				String password = FtnTools.generate8d();
				admin.setPassword(FtnTools.md5(password));
				ORMManager.get(WebAdmin.class).save(admin);
				// write netmail to primary address
				FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
						FtnTools.getPrimaryFtnAddress(), "HTTPD Module",
						"Sysop of Node", "Web password",
						"You can login to jNode site with those credentials: admin:"
								+ password + "\n");
			}
		} catch (RuntimeException e) {
		}
	}
}
