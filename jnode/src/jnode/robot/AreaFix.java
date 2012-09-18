package jnode.robot;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import jnode.dto.*;
import jnode.ftn.FtnAddress;
import jnode.ftn.FtnMessage;
import jnode.ftn.FtnTosser;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.orm.ORMManager;

/**
 * 
 * @author kreon
 * 
 */
public class AreaFix implements IRobot {
	private final static Logger logger = Logger.getLogger(AreaFix.class);

	private void writeReply(FtnMessage fmsg, String subject, String text)
			throws SQLException {

		Netmail netmail = new Netmail();
		netmail.setFromFTN(Main.info.getAddress().toString());
		netmail.setFromName("AreaFix");
		netmail.setToFTN(fmsg.getFromAddr().toString());
		netmail.setToName(fmsg.getFromName());
		netmail.setSubject(subject);
		netmail.setDate(new Date());
		StringBuilder sb = new StringBuilder();
		sb.append(String
				.format("\001REPLY: %s\n\001MSGID: %s %s\n\001PID: %s\001TID: %s\nHello, %s!\n\n",
						fmsg.getMsgid(), Main.info.getAddress().toString(),
						FtnTosser.generate8d(), Main.info.getVersion(),
						Main.info.getVersion(), netmail.getToName()));
		sb.append(text);
		sb.append("\n\n========== Original message ==========\n");
		sb.append(fmsg.getText().replaceAll("\001", "@"));
		sb.append("========== Original message ==========\n\n--- AreaFix "
				+ Main.info.getVersion() + "\n");
		netmail.setText(sb.toString());
		FtnMessage ret = new FtnMessage();
		ret.setFromAddr(new FtnAddress(Main.info.getAddress().toString()));
		ret.setToAddr(fmsg.getFromAddr());
		Link routeVia = FtnTosser.getRouting(ret);
		if (routeVia == null) {
			logger.error("Не могу найти роутинг для ответа на сообщение"
					+ fmsg.getMsgid());
			return;
		}
		netmail.setRouteVia(routeVia);
		ORMManager.netmail().create(netmail);
		logger.info("AreaFix создал Netmail #" + netmail.getId());
	}

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		Link link = null;
		{
			List<Link> links = ORMManager.link().queryForEq("ftn_address",
					fmsg.getFromAddr().toString());
			if (links.isEmpty()) {
				writeReply(fmsg, "Access denied",
						"You are not in links of origin");
				return;
			}
			link = links.get(0);
		}
		if (!link.getPaketPassword().equals(fmsg.getSubject())) {
			writeReply(fmsg, "Access denied", "Wrong password");
			return;
		}
		Pattern help = Pattern.compile("^%HELP$", Pattern.CASE_INSENSITIVE);
		Pattern list = Pattern.compile("^%LIST$", Pattern.CASE_INSENSITIVE);
		Pattern add = Pattern.compile("^%?\\+?(\\S+)$",
				Pattern.CASE_INSENSITIVE);
		Pattern rem = Pattern
				.compile("^%?\\-(\\S+)$", Pattern.CASE_INSENSITIVE);
		Pattern rescan = Pattern.compile("^%RESCAN (\\S+) (\\d+)$",
				Pattern.CASE_INSENSITIVE);
		Pattern add_rescan = Pattern.compile("^%?\\+?(\\S+) /r=(\\d+)$",
				Pattern.CASE_INSENSITIVE);
		StringBuilder reply = new StringBuilder();
		for (String line : fmsg.getText().split("\n")) {
			if (help.matcher(line).matches()) {
				writeReply(fmsg, "AreaFix help", help());
			} else if (list.matcher(line).matches()) {
				writeReply(fmsg, "AreaFix list", list(link));
			} else {
				Matcher m = rem.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					reply.append(rem(link, area));
					continue;
				}
				m = add.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					reply.append(add(link, area));
					continue;
				}
				m = rescan.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					int num = Integer.valueOf(m.group(2));
					reply.append(rescan(link, area, num));
					continue;
				}
				m = add_rescan.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					int num = Integer.valueOf(m.group(2));
					reply.append(add(link, area));
					reply.append(rescan(link, area, num));
					continue;
				}
			}
		}
		if (reply.length() > 0) {
			writeReply(fmsg, "AreaFix reply", reply.toString());
		}
	}

	/**
	 * Отправляем %HELP
	 * 
	 * @return
	 */
	private String help() {
		return "Avalible commands:\n" + "%HELP - this message\n"
				+ "%LIST - list of avalible areas\n"
				+ "+echo.area - subscribe echo.area\n"
				+ "-echo.area - unsibscribe echo.area"
				+ "+echo.area /r=N - subscribe and rescan N messages\n"
				+ "%RESCAN echo.area - rescan N messages";

	}

	/**
	 * Отправляем %LIST
	 * 
	 * @param link
	 * @return
	 * @throws SQLException
	 */
	private String list(Link link) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("Legend: * - subscribed\n\n========== List of echoareas ==========\n");
		List<Echoarea> areas = ORMManager.echoarea().queryForAll();
		for (Echoarea area : areas) {
			if (!ORMManager.subscription().queryBuilder().where()
					.eq("echoarea_id", area.getId()).and()
					.eq("link_id", link.getId()).query().isEmpty()) {
				sb.append("* ");
			} else {
				sb.append("  ");
			}
			sb.append(area.getName());
			if (area.getDescription().length() > 0) {
				sb.append(" - ");
				sb.append(area.getDescription());
			}
			sb.append('\n');
		}
		sb.append("========== List of echoareas ==========\n");
		return sb.toString();

	}

	private String add(Link link, String area) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		List<Echoarea> areas = ORMManager.echoarea().queryBuilder().where()
				.like("name", like).query();
		if (areas.isEmpty()) {
			sb.append(area + " not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				if (!ORMManager.subscription().queryBuilder().where()
						.eq("echoarea_id", earea).and().eq("link_id", link)
						.query().isEmpty()) {
					sb.append(" already subscribed");
				} else {
					Long lastid = 0L;
					String[] result = ORMManager
							.echoarea()
							.queryRaw(
									"SELECT max(id) FROM echomail WHERE echoarea_id="
											+ earea.getId()).getFirstResult();
					if (result[0] != null) {
						lastid = Long.valueOf(result[0]);
					}
					Subscription sub = new Subscription();
					sub.setArea(earea);
					sub.setLink(link);
					sub.setLast(lastid);
					ORMManager.subscription().create(sub);
					sb.append(" subscribed");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	private String rem(Link link, String area) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		List<Echoarea> areas = ORMManager.echoarea().queryBuilder().where()
				.like("name", like).query();
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				if (ORMManager.subscription().queryBuilder().where()
						.eq("echoarea_id", earea).and().eq("link_id", link)
						.query().isEmpty()) {
					sb.append(" is not subscribed");
				} else {
					DeleteBuilder<Subscription, ?> del = ORMManager
							.subscription().deleteBuilder();
					del.where().eq("link_id", link).and()
							.eq("echoarea_id", earea);
					ORMManager.subscription().delete(del.prepare());
					sb.append(" unsubscribed");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	private String rescan(Link link, String area, int num) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		List<Echoarea> areas = ORMManager.echoarea().queryBuilder().where()
				.like("name", like).query();
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				List<Subscription> sub = ORMManager.subscription()
						.queryBuilder().where().eq("echoarea_id", earea).and()
						.eq("link_id", link).query();
				if (sub.isEmpty()) {
					sb.append(" is not subscribed");
				} else {
					long last = sub.get(0).getLast();
					List<String[]> maxs = ORMManager
							.echomail()
							.queryRaw(
									"select id from echomail where echoarea_id="
											+ earea.getId()
											+ " order by id desc limit " + num)
							.getResults();
					int nums = 0;
					if (num > maxs.size()) {
						last = 0L;
					}
					for (String[] max : maxs) {
						try {
							long id = Long.valueOf(max[0]);
							if (id < last) {
								last = id;
							}
							{
								DeleteBuilder<Readsign, ?> db = ORMManager
										.readsign().deleteBuilder();
								db.where().eq("echomail_id", id).and()
										.eq("link_id", link);
								ORMManager.readsign().delete(db.prepare());
							}
							nums++;
						} catch (RuntimeException e) {
							e.printStackTrace();
						}
					}
					if (nums < num) {
						last = 0L;
					}
					{
						UpdateBuilder<Subscription, ?> upd = ORMManager
								.subscription().updateBuilder();
						if (last > 0) {
							last--;
						}
						upd.updateColumnValue("lastmessageid", last);
						upd.where().eq("link_id", link).and()
								.eq("echoarea_id", earea);
						ORMManager.subscription().update(upd.prepare());
					}
					sb.append(" rescanned " + nums + " messages");
				}
			}
		}
		sb.append('\n');
		return sb.toString();
	}
}
