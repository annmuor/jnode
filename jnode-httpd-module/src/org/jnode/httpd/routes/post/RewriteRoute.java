package org.jnode.httpd.routes.post;

import jnode.dto.Rewrite;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class RewriteRoute extends Route {

	public RewriteRoute() {
		super("/secure/rewrite");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String n = req.queryParams("n");
		String l = req.queryParams("l");
		String t = req.queryParams("t");
		String ofa = req.queryParams("ofa");
		String nfa = req.queryParams("nfa");
		String ofn = req.queryParams("ofn");
		String nfn = req.queryParams("nfn");
		String ota = req.queryParams("ota");
		String nta = req.queryParams("nta");
		String otn = req.queryParams("otn");
		String ntn = req.queryParams("ntn");
		String os = req.queryParams("os");
		String ns = req.queryParams("ns");

		String did = req.queryParams("did");

		String code = null;

		if (did != null) {
			try {
				Long id = Long.valueOf(did);
				Rewrite rew = ORMManager.get(Rewrite.class).getById(id);
				if (rew != null) {
					ORMManager.get(Rewrite.class).delete(rew);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				code = "ERROR";
			}
		} else {
			try {
				Long nice = Long.valueOf(n);
				boolean last = Boolean.valueOf(l);
				Rewrite rew = new Rewrite();
				rew.setNice(nice);
				rew.setLast(last);
				
				rew.setType(Rewrite.Type.valueOf(t));
				
				rew.setOrig_from_addr(ofa);
				rew.setNew_from_addr(nfa);

				rew.setOrig_from_name(ofn);
				rew.setNew_from_name(nfn);

				rew.setOrig_to_addr(ota);
				rew.setNew_to_addr(nta);

				rew.setOrig_to_name(otn);
				rew.setNew_to_name(ntn);

				rew.setOrig_subject(os);
				rew.setNew_subject(ns);

				ORMManager.get(Rewrite.class).save(rew);
			} catch (RuntimeException e) {
				e.printStackTrace();
				code = "ERROR";
			}
		}
		resp.header("Location", "/secure/rewrite.html"
				+ ((code != null) ? "?code=" + code : ""));
		halt(302);
		return null;
	}
}
