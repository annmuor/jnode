package org.jnode.httpd;

import jnode.event.IEvent;
import jnode.ftn.FtnTools;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;
import org.jnode.httpd.filters.SecureFilter;
import org.jnode.httpd.routes.get.EchoareasRoute;
import org.jnode.httpd.routes.get.FileareasRoute;
import org.jnode.httpd.routes.get.HealthRoute;
import org.jnode.httpd.routes.get.LinkoptionsRoute;
import org.jnode.httpd.routes.get.LinksRoute;
import org.jnode.httpd.routes.get.RoutingsRoute;
import org.jnode.httpd.routes.get.SelfRoute;
import org.jnode.httpd.routes.post.EchoareaRoute;
import org.jnode.httpd.routes.post.FileareaRoute;
import org.jnode.httpd.routes.post.LinkRequestRoute;
import org.jnode.httpd.routes.post.LinkRoute;
import org.jnode.httpd.routes.post.LinkoptionRoute;
import org.jnode.httpd.routes.post.MainRoute;
import org.jnode.httpd.routes.post.PointRequestRoute;
import org.jnode.httpd.routes.post.RoutingRoute;


/**
 * HttpdModule - модуль, слушающий порт и отдающий странички
 * 
 * @author kreon
 * 
 */
import spark.Spark;

@SuppressWarnings("unused")
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

		Spark.get(new LinkoptionsRoute("/secure/linkoptions"));

		Spark.post(new LinkoptionRoute("/secure/linkoption"));

		Spark.get(new EchoareasRoute("/secure/echoareas"));
		Spark.post(new EchoareaRoute("/secure/echoarea"));

		Spark.get(new FileareasRoute("/secure/fileareas"));
		Spark.post(new FileareaRoute("/secure/filearea"));

		Spark.get(new RoutingsRoute("/secure/routings"));
		Spark.post(new RoutingRoute("/secure/routing"));

		Spark.get(new HealthRoute("/secure/health"));
		// link request
		Spark.post(new LinkRequestRoute());
		// point request
		Spark.post(new PointRequestRoute());

		// final
//		Spark.get(new MainRoute());

		try {
			WebAdmin admin = ORMManager.get(WebAdmin.class).getFirstAnd();
			if (admin == null) {
				admin = new WebAdmin();
				admin.setUsername("admin");
				String password = FtnTools.generate8d();
				admin.setPassword(FtnTools.md5(password));
				ORMManager.get(WebAdmin.class).save(admin);
				// write netmail to primary address
				FtnTools.writeNetmail(
						FtnTools.getPrimaryFtnAddress(),
						FtnTools.getPrimaryFtnAddress(),
						"HTTPD Module",
						"Sysop of Node",
						"Web password",
						"You can login to jNode site with those credentials:\n  > login: admin\n > password "
								+ password + "\n");
			}
		} catch (RuntimeException e) {
		}
	}

	public static void main(String[] args) {
		for (String prop : System.getProperties().stringPropertyNames()) {
			System.out.println(prop + "=" + System.getProperty(prop));
		}
	}
}
