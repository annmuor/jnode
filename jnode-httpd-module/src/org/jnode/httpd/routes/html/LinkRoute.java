package org.jnode.httpd.routes.html;

import jnode.dto.Link;
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
		String host = req.queryParams("host");
		String _port = req.queryParams("port");
		try {
			
			Integer port = Integer.valueOf(_port);
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
			l.setProtocolHost(host);
			l.setProtocolPort(port);
			ORMManager.get(Link.class).saveOrUpdate(l);
			resp.header("Location", "/secure/links.html");
		} catch (RuntimeException e) {
			resp.header("Location", "/secure/links.html?error="+e.getMessage());
		}
		halt(302);
		return null;
	}
}
