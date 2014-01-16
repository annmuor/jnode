package org.jnode.httpd.routes.html;

import java.io.IOException;
import java.net.Socket;

import org.jnode.httpd.dto.LinkRequest;

import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;
import jnode.ndl.FtnNdlAddress;
import jnode.ndl.NodelistScanner;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class LinkRequestRoute extends Route {

	public LinkRequestRoute() {
		super("/linkrequest/:type");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String type = req.params(":type");
		String ret = "";
		if (!"confirm".equals(type)) {
			String addr = req.queryParams("addr");
			String host = req.queryParams("host");
			String port = req.queryParams("port");
			if (addr != null && host != null && port != null) {
				try {
					FtnAddress ftn = new FtnAddress(addr);
					FtnNdlAddress ndl = NodelistScanner.getInstance().isExists(
							ftn);
					boolean exists = (ndl != null);
					Integer iport = Integer.valueOf(port);
					boolean connected = false;
					if (!exists) {
						ret += "Your ftn is not exists in nodelist";
					}
					if ("-".equals(host) || iport.intValue() == 0) {
						connected = true;
						host = "-";
						iport = 0;
					} else {
						try {
							Socket sock = new Socket(host, iport);
							sock.close();
							connected = true;
						} catch (IOException e) {
							ret += host
									+ ":"
									+ iport
									+ " is not allowed to connect to; Use -:0 instead if you are PVT node";
						}
					}
					if (connected && exists) {
						String name = ndl.getName() != null ? ndl.getName().replace('_', ' ') : addr;
						// create link and send netmail
						LinkRequest lr = new LinkRequest();
						lr.setAddress(addr);
						lr.setHost(host);
						lr.setPort(iport);
						lr.setName(name);
						synchronized (LinkRequest.class) {
							LinkRequest lr2 = ORMManager.get(LinkRequest.class)
									.getFirstAnd("address", "=", addr);
							if (lr2 == null) {
								String akey = FtnTools.generate8d();
								lr.setAkey(akey);
								ORMManager.get(LinkRequest.class).save(lr);
								writeKey(lr);
								ret = "Check you netmail for future instructions";
							}
						}
					}
				} catch (RuntimeException e) {
					ret += "Invalid request (e)";
				}
			} else {
				ret += "Invalid request";
			}
		} else {
			String akey = req.queryParams("key");
			String id = req.queryParams("id");
			try {
				LinkRequest lr = ORMManager.get(LinkRequest.class).getById(id);
				if (lr != null && lr.getAkey().equals(akey)) { // valid
					String password = FtnTools.generate8d();
					Link link = new Link();
					link.setLinkName(lr.getName());
					link.setLinkAddress(lr.getAddress());
					link.setProtocolHost(lr.getHost());
					link.setProtocolPort(lr.getPort());
					link.setPaketPassword(password);
					link.setProtocolPassword(password);
					// check
					synchronized (Link.class) {
						Link l2 = ORMManager.get(Link.class).getFirstAnd(
								"ftn_address", "=", link.getLinkAddress());
						if (l2 == null) {
							ORMManager.get(Link.class).save(link);
							ORMManager.get(LinkRequest.class).delete(lr);
							writeGreets(link);
							ret = "Your connection/packet password is "
									+ password
									+ "; You are welcome to test your connection now :-)";
						} else {
							ret = "Sorry, this link already exists in database";
						}
					}
				} else {
					ret = "Invalid request's id or key";
				}
			} catch (RuntimeException e) {
				ret = "Invalid request";
			}
		}
		return html(ret);
	}

	private String html(String string) {
		return "<html><head><title>jNode::Web</title></head><body>Response: "
				+ string
				+ "<BR/><a href=\"/index.html\">Main page</a><BR/><hr width=\"100%\" />"
				+ "<small>Powered by <a href=\"https://github.com/kreon/jnode\">jNode</a></small>"
				+ "</body></html>";
	}

	private void writeGreets(Link link) {
		// написать нам на почту
		FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
				FtnTools.getPrimaryFtnAddress(), MainHandler
						.getCurrentInstance().getInfo().getStationName(),
				MainHandler.getCurrentInstance().getInfo().getSysop(),
				"New linkage", "New link with " + link.getLinkAddress()
						+ " completed");

		FtnTools.writeNetmail(
				FtnTools.getPrimaryFtnAddress(),
				new FtnAddress(link.getLinkAddress()),
				MainHandler.getCurrentInstance().getInfo().getStationName(),
				link.getLinkName(),
				"You are welcome",
				"You are now have linkage with our node.\n"
						+ "Please, follow the Fidonet rules and keep your connection stable");
	}

	private void writeKey(LinkRequest lr) {
		// написать нам на почту
		FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
				FtnTools.getPrimaryFtnAddress(), MainHandler
						.getCurrentInstance().getInfo().getStationName(),
				MainHandler.getCurrentInstance().getInfo().getSysop(),
				"New linkage request",
				"New link request from " + lr.getAddress() + " accepted");

		FtnTools.writeNetmail(
				FtnTools.getPrimaryFtnAddress(),
				new FtnAddress(lr.getAddress()),
				MainHandler.getCurrentInstance().getInfo().getStationName(),
				lr.getName(),
				"Link instructions",
				"Somebody have just started a linkage proccess from your address.\n"
						+ "If this was you, visit /confirmlink.html on our site and fill fields as described below:\n"
						+ " > Request Id: " + lr.getId() + "\n"
						+ " > Request Key: " + lr.getAkey() + "\n");
	}

}
