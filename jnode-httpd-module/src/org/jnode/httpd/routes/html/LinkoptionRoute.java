package org.jnode.httpd.routes.html;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.orm.ORMManager;
import spark.Request;
import spark.Response;
import spark.Route;

public class LinkoptionRoute extends Route {

	public LinkoptionRoute(String path) {
		super(path);
	}

	@Override
	public Object handle(Request req, Response resp) {
		String id = req.queryParams("_id");
		if (id != null) {
			try {
				Long lid = Long.valueOf(id);
				Link link = ORMManager.get(Link.class).getById(lid);
				if (link != null) {
					for (String name : req.queryParams()) {
						if (name.startsWith("_")) {
							continue;
						}
						LinkOption option = ORMManager.get(LinkOption.class)
								.getFirstAnd("link_id", "=", link, "name", "=",
										name);
						String value = req.queryParams(name);
						if (value != null && value.length() > 0) {
							if (option == null) {
								option = new LinkOption();
								option.setLink(link);
								option.setOption(name);
							}
							option.setValue(value);
							ORMManager.get(LinkOption.class).saveOrUpdate(
									option);
						} else {
							if (option != null) {
								ORMManager.get(LinkOption.class).delete(option);
							}
						}
					}
				}
			} catch (RuntimeException e) {
			}
		}
		resp.header("Location", "/secure/links.html");
		halt(302);
		return null;
	}
}
