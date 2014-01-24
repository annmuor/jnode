package org.jnode.httpd.routes.post;

import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class LinkRoute extends Route {
	public LinkRoute(String path) {
		super(path);
	}

	@Override
	public Object handle(Request req, Response resp) {
		String _id = req.queryParams("id");
		String name = req.queryParams("name");
		String _ftn = req.queryParams("addr");
		String pass = req.queryParams("password");
		String pktpass = req.queryParams("pktpassword");
		String address = req.queryParams("address");
		String code = null;
		String delete = req.queryParams("did");
		if (delete != null) {
			try {
				Long eid = Long.valueOf(delete);
				Link deleteLink = ORMManager.get(Link.class).getById(eid);
				if (deleteLink != null) {
					FtnTools.delete(deleteLink);
				}
			} catch (RuntimeException e) {
				code = "ERROR";
			}
		} else {
			try {
				FtnAddress ftn = new FtnAddress(_ftn);
				Link l = null;
				if (!_id.equals("0")) {
					Long id = Long.valueOf(_id);
					l = ORMManager.get(Link.class).getById(id);
				}
				if (l == null) {
					l = new Link();
				}
				l.setLinkAddress(ftn.toString());
				l.setLinkName(name);
				l.setPaketPassword(pktpass);
				l.setProtocolPassword(pass);
				l.setProtocolAddress(address);
				ORMManager.get(Link.class).saveOrUpdate(l);
			} catch (RuntimeException e) {
				code = "ERROR";
			}
		}
		resp.header("Location", "/secure/links.html"
				+ ((code != null) ? "?error=" + code : ""));
		halt(302);
		return null;
	}
}
