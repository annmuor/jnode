package org.jnode.xmpp.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.dao.GenericDAO;
import jnode.dto.Echoarea;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Route;
import jnode.dto.Subscription;
import jnode.orm.ORMManager;

/**
 * Команда LIST
 * 
 * @author kreon
 * 
 */
public class ListCommandProcessor implements CommandProcessor {
	/**
	 * LIST <area [name=*]|link [ftn=*]|routing [via=*]|subscription
	 * [ftn=*|echo=*]> [limit=N]
	 */
	private static final Pattern echoarea = Pattern.compile(
			"LIST[ ]+area([ ]+name=(?<name>.+?))?([ ]+limit=(?<limit>\\d+))?",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern link = Pattern.compile(
			"LIST[ ]+link([ ]+ftn=(?<ftn>.+?))?([ ]+limit=(?<limit>\\d+))?",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern routing = Pattern.compile(
			"LIST[ ]+routing([ ]+via=(?<via>.+?))?([ ]+limit=(?<limit>\\d+))?",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern subscription = Pattern
			.compile(
					"LIST[ ]+subscription([ ]+ftn=(?<ftn>.+?))?([ ]+echo=(?<echo>.+?))?([ ]+limit=(?<limit>\\d+))?",
					Pattern.CASE_INSENSITIVE);

	@Override
	public String process(String command) {
		Matcher m = echoarea.matcher(command);
		if (m.matches()) {
			String name = m.group("name");
			int limit = getLimit(m);
			return join("----------- Echoareas list",
					listEchoareas(name, limit), "-----------  EOF");
		}
		m = link.matcher(command);
		if (m.matches()) {
			String ftn = m.group("ftn");
			int limit = getLimit(m);
			return join("----------- Links list", listLinks(ftn, limit),
					"-----------  EOF");
		}
		m = routing.matcher(command);
		if (m.matches()) {
			String via = m.group("via");
			int limit = getLimit(m);
			return join("----------- Routing rules", listRouting(via, limit),
					"-----------  EOF");
		}
		m = subscription.matcher(command);
		if (m.matches()) {
			String ftn = m.group("ftn");
			String echo = m.group("echo");
			int limit = getLimit(m);
			return join("----------- Subscription list",
					listSubscription(ftn, echo, limit), "-----------  EOF");
		}

		return "Unknown LIST args; Type HELP to see valid args";
	}

	private int getLimit(Matcher m) {
		String sLimit = m.group("limit");
		int limit = (sLimit != null) ? new Integer(sLimit) : 0;
		return limit;
	}

	private String join(String header, List<String> array, String footer) {
		StringBuilder sb = new StringBuilder();
		if (header != null) {
			sb.append(header);
			sb.append('\n');
		}
		for (String s : array) {
			sb.append(s);
			sb.append('\n');
		}
		if (footer != null) {
			sb.append(footer);
			sb.append('\n');
		}
		return sb.toString();
	}

	private List<String> listEchoareas(String name, int limit) {
		GenericDAO<Echoarea> echoDAO = ORMManager.INSTANSE.getEchoareaDAO();
		if (name != null) {
			name = name.replace('*', '%');
		}
		List<Echoarea> areas = (name != null) ? (limit != 0) ? echoDAO
				.getOrderLimitAnd(limit, "name", true, "name", "like", name)
				: echoDAO.getOrderAnd("name", true, "name", "like", name)
				: (limit != 0) ? echoDAO.getOrderLimitAnd(limit, "name", true)
						: echoDAO.getOrderAnd("name", true);
		List<String> ret = new ArrayList<String>(areas.size());
		for (Echoarea a : areas) {
			ret.add(String
					.format("Echoarea %s\n Descr: %s\n ReadLvl: %d\n WriteLvl: %d\n Group: %s\n",
							a.getName(), a.getDescription(), a.getReadlevel(),
							a.getWritelevel(), a.getGroup()));
		}
		return ret;
	}

	private List<String> listLinks(String ftn_address, int limit) {
		GenericDAO<Link> linkDAO = ORMManager.INSTANSE.getLinkDAO();
		GenericDAO<LinkOption> linkOptionDAO = ORMManager.INSTANSE
				.getLinkOptionDAO();
		if (ftn_address != null) {
			ftn_address = ftn_address.replace('*', '%');
		}
		List<String> ret = new ArrayList<String>();
		List<Link> links = (ftn_address != null) ? (limit != 0) ? linkDAO
				.getOrderLimitAnd(limit, "ftn_address", true, "ftn_address",
						"like", ftn_address) : linkDAO.getOrderAnd(
				"ftn_address", true, "ftn_address", "like", ftn_address)
				: (limit != 0) ? linkDAO.getOrderLimitAnd(limit, "ftn_address",
						true) : linkDAO.getOrderAnd("ftn_address", true);
		for (Link l : links) {
			List<LinkOption> options = linkOptionDAO.getAnd("link_id", "eq",
					l.getId());
			StringBuilder opts = new StringBuilder();
			int idx = 0;
			for (LinkOption o : options) {
				if (idx > 0) {
					opts.append(", ");
				}
				opts.append(LinkOption
						.getOptByName(o.getOption().toLowerCase()));
				opts.append(":");
				opts.append(o.getValue());
				idx++;
			}
			ret.add(String
					.format("Link %s\n FTN: %s\n PktPWd: %s\n BinkPwd: %s\n INA: %s:%d\n Opts: %s\n",
							l.getLinkName(), l.getLinkAddress(),
							l.getPaketPassword(), l.getProtocolPassword(),
							l.getProtocolHost(), l.getProtocolPort(),
							opts.toString()));
		}
		return ret;
	}

	private List<String> listRouting(String via, int limit) {
		List<Route> routing = null;
		GenericDAO<Route> routeDao = ORMManager.INSTANSE.getRouteDAO();
		GenericDAO<Link> linkDAO = ORMManager.INSTANSE.getLinkDAO();
		if (via != null) {
			Link link = linkDAO.getFirstAnd("ftn_address", "eq", via);
			routing = (limit != 0) ? routeDao.getOrderLimitAnd(limit, "nice",
					true, "route_via", "eq", link) : routeDao.getOrderAnd(
					"nice", true, "route_via", "eq", link);

		} else {
			routing = (limit != 0) ? routeDao.getOrderLimitAnd(limit, "nice",
					true) : routeDao.getOrderAnd("nice", true);
		}
		List<String> ret = new ArrayList<String>();
		for (Route r : routing) {
			r.setRouteVia(linkDAO.getById(r.getRouteVia().getId()));
			ret.add(String
					.format("Routing %d\n FromName: %s\n FromAddr: %s\n ToName: %s\n ToAddr: %s\n Subject: %s\n RouteVia: %s\n",
							r.getNice(), r.getFromName(), r.getFromAddr(), r
									.getToName(), r.getToAddr(),
							r.getSubject(), r.getRouteVia().getLinkAddress()));
		}
		return ret;
	}

	private List<String> listSubscription(String ftn, String echo, int limit) {
		GenericDAO<Link> linkDAO = ORMManager.INSTANSE.getLinkDAO();
		GenericDAO<Echoarea> echoDAO = ORMManager.INSTANSE.getEchoareaDAO();
		GenericDAO<Subscription> subDAO = ORMManager.INSTANSE
				.getSubscriptionDAO();
		List<Object> va_args = new ArrayList<Object>();
		if (ftn != null) {
			Link link = linkDAO.getFirstAnd("ftn_address", "eq", ftn);
			va_args.add("link_id");
			va_args.add("eq");
			va_args.add(link);
		}

		if (echo != null) {
			Echoarea area = echoDAO.getFirstAnd("name", "eq", echo);
			va_args.add("echoarea_id");
			va_args.add("eq");
			va_args.add(area);
		}
		Object[] args = va_args.toArray(new Object[] {});
		List<Subscription> subs = (limit != 0) ? subDAO.getOrderLimitAnd(limit,
				"link_id", true, args) : subDAO.getOrderAnd("link_id", true,
				args);
		List<String> ret = new ArrayList<String>();
		for (Subscription s : subs) {
			s.setArea(echoDAO.getById(s.getArea().getId()));
			s.setLink(linkDAO.getById(s.getLink().getId()));

			ret.add(String.format("Link %s subscribed on %s", s.getLink()
					.getLinkAddress(), s.getArea().getName()));
		}
		return ret;
	}
}
