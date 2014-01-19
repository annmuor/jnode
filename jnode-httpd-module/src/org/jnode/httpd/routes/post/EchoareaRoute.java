package org.jnode.httpd.routes.post;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class EchoareaRoute extends Route {

	public EchoareaRoute(String path) {
		super(path);
	}

	@Override
	public Object handle(Request req, Response resp) {
		String id = req.queryParams("id");
		String name = req.queryParams("name");
		String descr = req.queryParams("descr");
		String rl = req.queryParams("rl");
		String wl = req.queryParams("wl");
		String gr = req.queryParams("gr");
		String code = null;
		String delete = req.queryParams("did");
		if (delete != null) {
			try {
				Long eid = Long.valueOf(delete);
				Echoarea deleteArea = ORMManager.get(Echoarea.class).getById(
						eid);
				if (deleteArea != null) {
					FtnTools.delete(deleteArea);
				}
			} catch (RuntimeException e) {
				code = "ERROR";
			}
		} else {
			try {
				Echoarea ea;
				if (!name.matches("^[-a-zA-Z0-9_\\.]+$")) {
					code = "ENAME";
				} else {
					if (id == null || "0".equals(id)) {
						ea = new Echoarea();
						ea.setName(name);
					} else {
						Long eid = Long.valueOf(id);
						ea = ORMManager.get(Echoarea.class).getById(eid);
					}
					ea.setDescription(descr);
					ea.setReadlevel(Long.valueOf(rl));
					ea.setWritelevel(Long.valueOf(wl));
					ea.setGroup(gr);
					synchronized (Echoarea.class) {
						if (ea.getId() == null
								&& ORMManager.get(Echoarea.class).getFirstAnd(
										"name", "=", ea.getName()) != null) {
							code = "EXISTS";
						} else {
							ORMManager.get(Echoarea.class).saveOrUpdate(ea);
						}
					}
				}
			} catch (RuntimeException e) {
				code = "INVALID";
			}
		}
		resp.header("Location", "/secure/echoes.html"
				+ ((code != null) ? "?code=" + code : ""));
		halt(302);
		return null;
	}

}
