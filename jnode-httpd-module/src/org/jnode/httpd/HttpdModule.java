package org.jnode.httpd;

import jnode.event.IEvent;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;
import org.jnode.httpd.filters.*;
import org.jnode.httpd.routes.get.*;
import org.jnode.httpd.routes.post.*;

/**
 * HttpdModule - модуль, слушающий порт и отдающий странички
 * 
 * @author kreon
 * 
 */
import spark.Spark;

public class HttpdModule extends JnodeModule {
	private static final String CONFIG_PORT = "port";
	private static final Logger logger = Logger.getLogger(HttpdModule.class);
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

		Spark.get(new SelfRoute());
		Spark.get(new SelfRoute("/"));
		Spark.get(new SelfRoute(""));
		Spark.get(new HealthRoute());
		Spark.get(new LinksRoute());
		Spark.get(new BecomePointRoute());
		Spark.get(new LinkoptionsRoute());
		Spark.get(new EchoareasRoute());
		Spark.get(new FileareasRoute());
		Spark.get(new RoutingsRoute());
		Spark.get(new PointRequestConfirmRoute());

		Spark.post(new LinkRoute("/secure/link"));
		Spark.post(new LinkoptionRoute("/secure/linkoption"));
		Spark.post(new EchoareaRoute("/secure/echoarea"));
		Spark.post(new FileareaRoute("/secure/filearea"));
		Spark.post(new RoutingRoute("/secure/routing"));

		Spark.post(new LinkRequestRoute());
		Spark.post(new PointRequestRoute());

		try {
			WebAdmin admin = ORMManager.get(WebAdmin.class).getFirstAnd();
			if (admin == null) {
				admin = new WebAdmin();
				admin.setUsername("admin");
				String password = FtnTools.generate8d();
				admin.setPassword(FtnTools.md5(password));
				ORMManager.get(WebAdmin.class).save(admin);
				String text = "You can login to jNode site with those credentials:\n  > login: admin\n > password "
						+ password + "\n";
				// write netmail to primary address
				FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
						FtnTools.getPrimaryFtnAddress(), "HTTPD Module",
						"Sysop of Node", "Web password", text);
				logger.l1("Admin created\n" + text);
			}
		} catch (RuntimeException e) {
		}
	}
}
