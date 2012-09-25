package jnode.robot;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import jnode.dto.*;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.orm.ORMManager;

/**
 * 
 * @author kreon
 * 
 */
public class AreaFix implements IRobot {
	private static final Pattern help = Pattern.compile("^%HELP$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern list = Pattern.compile("^%LIST$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern query = Pattern.compile("^%QUERY$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern add = Pattern.compile("^%?\\+?(\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern rem = Pattern.compile("^%?\\-(\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern rescan = Pattern.compile(
			"^%RESCAN (\\S+) (\\d+)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern add_rescan = Pattern.compile(
			"^%?\\+?(\\S+) /r=(\\d+)$", Pattern.CASE_INSENSITIVE);

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		Link link = null;
		{
			List<Link> links = ORMManager.INSTANSE.link().queryForEq(
					"ftn_address", fmsg.getFromAddr().toString());
			if (links.isEmpty()) {
				FtnTools.writeReply(fmsg, "Access denied",
						"You are not in links of origin");
				return;
			}
			link = links.get(0);
		}
		if (!FtnTools.getOptionBooleanDefTrue(link, LinkOption.BOOLEAN_AREAFIX)) {
			FtnTools.writeReply(fmsg, "You are not welcome",
					"Sorry, AreaFix is off for you");
			return;
		}
		if (!link.getPaketPassword().equals(fmsg.getSubject())) {
			FtnTools.writeReply(fmsg, "Access denied", "Wrong password");
			return;
		}

		StringBuilder reply = new StringBuilder();
		for (String line : fmsg.getText().split("\n")) {
			line = line.toLowerCase();
			if (help.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "AreaFix help", help());
			} else if (list.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "AreaFix list", list(link));
			} else if (query.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "AreaFix query", query(link));
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
			FtnTools.writeReply(fmsg, "AreaFix reply", reply.toString());
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
				+ "%QUERY - list of subscribed areas\n"
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
		List<Echoarea> areas = ORMManager.INSTANSE.echoarea().queryBuilder()
				.orderBy("name", true).query();
		for (Echoarea area : areas) {
			if (!ORMManager.INSTANSE.subscription().queryBuilder().where()
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

	/**
	 * Отправляем %LIST
	 * 
	 * @param link
	 * @return
	 * @throws SQLException
	 */
	private String query(Link link) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("========== List of subscribed areas ==========\n");
		GenericRawResults<String[]> echoes = ORMManager.INSTANSE
				.echoarea()
				.queryRaw(
						String.format(
								"SELECT a.name,a.description FROM subscription s"
										+ " RIGHT JOIN echoarea a on (a.id=s.echoarea_id)"
										+ " WHERE s.link_id=%d ORDER BY a.name",
								link.getId()));
		for (String[] echo : echoes.getResults()) {
			sb.append("* ");
			sb.append(echo[0]);
			if (echo[1].length() > 0) {
				sb.append(" - ");
				sb.append(echo[1]);
			}
			sb.append('\n');
		}
		sb.append("========== List of subscribed areas ==========\n");
		return sb.toString();

	}

	private String add(Link link, String area) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		String[] grps = FtnTools.getOptionStringArray(link,
				LinkOption.SARRAY_LINK_GROUPS);
		List<Echoarea> areas = ORMManager.INSTANSE.echoarea().queryBuilder()
				.where().like("name", like).query();
		if (areas.isEmpty()) {
			sb.append(area + " not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				if (!ORMManager.INSTANSE.subscription().queryBuilder().where()
						.eq("echoarea_id", earea).and().eq("link_id", link)
						.query().isEmpty()) {
					sb.append(" already subscribed");
				} else {
					boolean denied = true;
					if (!"".equals(earea.getGroup())) {
						for (String group : grps) {
							if (earea.getGroup().equals(group)) {
								denied = false;
								break;
							}
						}
					} else {
						denied = false;
					}
					if (denied) {
						sb.append(" access denied");
					} else {
						Long lastid = 0L;
						String[] result = ORMManager.INSTANSE
								.echoarea()
								.queryRaw(
										"SELECT max(id) FROM echomail WHERE echoarea_id="
												+ earea.getId())
								.getFirstResult();
						if (result[0] != null) {
							lastid = Long.valueOf(result[0]);
						}
						Subscription sub = new Subscription();
						sub.setArea(earea);
						sub.setLink(link);
						sub.setLast(lastid);
						ORMManager.INSTANSE.subscription().create(sub);
						sb.append(" subscribed");
					}
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
		List<Echoarea> areas = ORMManager.INSTANSE.echoarea().queryBuilder()
				.where().like("name", like).query();
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				if (ORMManager.INSTANSE.subscription().queryBuilder().where()
						.eq("echoarea_id", earea).and().eq("link_id", link)
						.query().isEmpty()) {
					sb.append(" is not subscribed");
				} else {
					DeleteBuilder<Subscription, ?> del = ORMManager.INSTANSE
							.subscription().deleteBuilder();
					del.where().eq("link_id", link).and()
							.eq("echoarea_id", earea);
					ORMManager.INSTANSE.subscription().delete(del.prepare());
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
		List<Echoarea> areas = ORMManager.INSTANSE.echoarea().queryBuilder()
				.where().like("name", like).query();
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				List<Subscription> sub = ORMManager.INSTANSE.subscription()
						.queryBuilder().where().eq("echoarea_id", earea).and()
						.eq("link_id", link).query();
				if (sub.isEmpty()) {
					sb.append(" is not subscribed");
				} else {
					long last = sub.get(0).getLast();
					List<String[]> maxs = ORMManager.INSTANSE
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
								DeleteBuilder<Readsign, ?> db = ORMManager.INSTANSE
										.readsign().deleteBuilder();
								db.where().eq("echomail_id", id).and()
										.eq("link_id", link);
								ORMManager.INSTANSE.readsign().delete(
										db.prepare());
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
						UpdateBuilder<Subscription, ?> upd = ORMManager.INSTANSE
								.subscription().updateBuilder();
						if (last > 0) {
							last--;
						}
						upd.updateColumnValue("lastmessageid", last);
						upd.where().eq("link_id", link).and()
								.eq("echoarea_id", earea);
						ORMManager.INSTANSE.subscription()
								.update(upd.prepare());
					}
					sb.append(" rescanned " + nums + " messages");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}
}
