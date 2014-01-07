package org.jnode.httpd;

import org.jnode.httpd.dao.PointRequestDAO;
import org.jnode.httpd.dto.PointRequest;

import jnode.dto.Link;
import jnode.event.IEvent;
import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;
/**
 * HttpdModule - модуль, слушающий порт и отдающий странички
 * 
 * @author kreon
 * 
 */
import spark.*;

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
		
		Spark.get(new Route("/self/address/list") {

			@Override
			public Object handle(Request req, Response resp) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				boolean f = true;
				for (FtnAddress address : MainHandler.getCurrentInstance()
						.getInfo().getAddressList()) {
					if (f) {
						f = false;
					} else {
						sb.append(",");
					}
					sb.append("\"" + address.toString() + "\"");
				}
				resp.type("application/json");
				return sb.toString();
			}
		});
		Spark.post(new Route("/point/new") {

			@Override
			public Object handle(Request req, Response resp) {
				String node = req.queryParams("node");
				String point = req.queryParams("point");
				String name = req.queryParams("name");
				String email = req.queryParams("email");
				// check point not exists
				try {
					FtnAddress address = new FtnAddress(node + "." + point);
					boolean isPoint = false;
					for (FtnAddress me : MainHandler.getCurrentInstance()
							.getInfo().getAddressList()) {
						if (address.isPointOf(me)) {
							isPoint = true;
							break;
						}
					}
					if (!isPoint) {
						throw new RuntimeException();
					}
					Link test = ORMManager.INSTANSE.getLinkDAO().getFirstAnd(
							"ftn_address", "=", address.toString());
					if (test != null) {
						throw new RuntimeException();
					}
					PointRequest request = new PointRequest();
					request.setFtnAddress(address.toString());
					request.setEmail(email);
					request.setName(name);
					PointRequestDAO.getSelf().save(request);
					resp.redirect("/newpointwelcome.html");
				} catch (RuntimeException e) {
					resp.redirect("/newpoint.html?error=INVALID_POINT");
				}
				return null;
			}
		});

	}
}
