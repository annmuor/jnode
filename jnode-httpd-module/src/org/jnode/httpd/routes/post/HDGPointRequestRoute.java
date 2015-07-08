package org.jnode.httpd.routes.post;

import org.jnode.httpd.HttpdModule;
import org.jnode.httpd.dto.PointRequest;

import jnode.dto.Echoarea;
import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class HDGPointRequestRoute extends Route {

	private boolean enabled = false;

	public HDGPointRequestRoute(boolean enabled) {
		super("/hdgpntrequest");
		this.enabled = enabled;
	}

	@Override
	public Object handle(Request req, Response resp) {
		if (!enabled) {
			return "ERROR\r\nAUTOPOINT DISABLED\r\n";
		}
		String name = req.queryParams("_name");
		String email = req.queryParams("_email");
		String password = req.queryParams("_password");
		String about = req.queryParams("_about");
		String error = "";
		// check this shit
		{
			if (!name.matches("^[A-Z][a-z]+ [A-Z][a-z]+$")) {
				error += "NAME_CHECK_FAILED ";
			}
			if (!email
					.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
				error += "MAIL_CHECK_FAILED ";
			}
			if (password.length() < 8) {
				error += "MIN_PASSWD_LEN=8 ";
			}
			if (!(password.matches(".*[0-9].*")
					&& password.matches(".*[A-Z].*") && password
						.matches(".*[a-z].*"))) {
				error += "WEAK_PASSWD ";
			}
			if (error.length() > 0) {
				error(error);
				return "ERROR\r\n" + error;
			}
		}
		// seems ok
		FtnAddress guessedAddress = guessNewPointAddress();
		if (guessedAddress == null) {
			error("NO_PNT_ADDRESS_SPACE");
			return "ERROR\r\nNO_PNT_ADDRESS_SPACE";
		}
		// do point request
		PointRequest pReq = new PointRequest();
		pReq.setAddr(guessedAddress.toString());
		pReq.setEmail(email);
		pReq.setName(name);
		pReq.setPassword(password);
		// save
		ORMManager.get(PointRequest.class).save(pReq);
		// create link
		Link link = new Link();
		link.setLinkAddress(guessedAddress.toString());
		link.setLinkName(name);
		link.setProtocolAddress("-");
		link.setProtocolPassword(password);
		link.setPaketPassword(password);
		ORMManager.get(Link.class).save(link);
		// write echomail
		{
			String techArea = MainHandler.getCurrentInstance().getProperty(
					"stat.area", null);
			String text = guessedAddress + "," + name + "," + about;
			if (techArea != null) {
				Echoarea area = FtnTools.getAreaByName(techArea, null);
				FtnTools.writeEchomail(area, "New HTDGPoint", text);
			}
			FtnTools.writeNetmail(guessedAddress,
					FtnTools.getPrimaryFtnAddress(), MainHandler
							.getCurrentInstance().getInfo().getStationName(),
					MainHandler.getCurrentInstance().getInfo().getSysop(),
					"New HTDG point", text);
			ok(text);
		}
		return "OK\r\n" + guessedAddress;
	}

	private void error(String error) {
		HttpdModule.logger.l2("HDGPRError: " + error);
	}

	private void ok(String ok) {
		HttpdModule.logger.l3("HDGPR: " + ok);
	}

	private FtnAddress guessNewPointAddress() {
		FtnAddress baseNodeAddr = FtnTools.getPrimaryFtnAddress().clone();
		if (baseNodeAddr.getPoint() != 0) {
			return null;
		}
		// from 100 to 10000
		for (int i = 100; i < 10000; i++) {
			baseNodeAddr.setPoint(i);
			Link l = FtnTools.getLinkByFtnAddress(baseNodeAddr);
			if (l == null) {
				return baseNodeAddr;
			}
		}
		return null;
	}
}
