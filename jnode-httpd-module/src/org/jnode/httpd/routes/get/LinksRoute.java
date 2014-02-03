package org.jnode.httpd.routes.get;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jnode.dto.Link;
import jnode.ftn.types.FtnAddress;
import jnode.orm.ORMManager;

import org.jnode.httpd.util.HTML;
import org.jnode.httpd.util.JSONUtil;

import spark.Request;
import spark.Response;
import spark.Route;

public class LinksRoute extends Route {
	private static String links = null;

	public LinksRoute() {
		super("/secure/links.html");
		if (links == null) {
			links = HTML.getContents("/parts/links.html");
		}
	}

	@Override
	public Object handle(Request req, Response resp) {
		StringBuilder sb = new StringBuilder();
		String id = req.queryParams("id");
		if (id != null) {
			try {
				String cb = req.queryParams("cb");
				if (cb != null) {
					sb.append(cb + "(");
				}
				Long lid = Long.valueOf(id);
				Link l = ORMManager.get(Link.class).getById(lid);
				sb.append(JSONUtil.value(l));
				if (cb != null) {
					sb.append(")");
				}
				resp.type("text/javascript");
				return sb.toString();
			} catch (NumberFormatException e) {

			}
		} else {
			List<Link> links = ORMManager.get(Link.class).getAll();
			sortLinks(links);
			for (Link object : links) {
				sb.append(String
						.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td><a href=\"#edit\" class=\"css-link-1\" onclick=\"edit(%d)\">Изменить</a>&nbsp;<a href=\"#options\" class=\"css-link-1\" onclick=\"options(%d)\">Настройки</a>&nbsp;<a href=\"#\" class=\"css-link-1\" onclick=\"del(%d)\">Удалить</a></td></tr>",
								object.getLinkName(), object.getLinkAddress(),
								object.getProtocolAddress(),
								object.getProtocolPassword(),
								object.getPaketPassword(), object.getId(),
								object.getId(), object.getId()));
			}
			return HTML.start(true)
					.append(String.format(LinksRoute.links, sb.toString()))
					.footer().get();
		}
		return null;
	}

	private void sortLinks(List<Link> links) {
		Collections.sort(links, new Comparator<Link>() {

			@Override
			public int compare(Link o1, Link o2) {
				FtnAddress a1 = new FtnAddress(o1.getLinkAddress());
				FtnAddress a2 = new FtnAddress(o2.getLinkAddress());
				if (a1.getPoint() > 0) {
					if (a2.getPoint() > 0) {
						return a1.getPoint() - a2.getPoint();
					} else {
						return 1;
					}
				}
				if (a2.getPoint() > 0) {
					if (a1.getPoint() > 0) {
						return a1.getPoint() - a2.getPoint();
					} else {
						return -1;
					}
				}
				if (a1.getZone() == a2.getZone()) {
					if (a1.getNet() == a2.getNet()) {
						if (a1.getNode() == a2.getNode()) {
							return 0;
						} else {
							return a1.getNode() - a2.getNode();
						}
					} else {
						return a1.getNet() - a2.getNet();
					}
				} else {
					return a1.getZone() - a2.getZone();
				}
			}

		});

	}
}
