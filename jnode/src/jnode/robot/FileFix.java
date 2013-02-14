package jnode.robot;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.dao.GenericRawResults;

import jnode.dto.FileSubscription;
import jnode.dto.Filearea;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.orm.ORMManager;

/**
 * Простой filefix
 * 
 * @author kreon
 * 
 */
public class FileFix implements IRobot {
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

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		Link link = null;
		{
			List<Link> links = ORMManager.INSTANSE.getLinkDAO().getAnd(
					"ftn_address", "=", fmsg.getFromAddr().toString());
			if (links.isEmpty()) {
				FtnTools.writeReply(fmsg, "Access denied",
						"You are not in links of origin");
				return;
			}
			link = links.get(0);
		}
		if (!FtnTools.getOptionBooleanDefTrue(link, LinkOption.BOOLEAN_FILEFIX)) {
			FtnTools.writeReply(fmsg, "You are not welcome",
					"Sorry, AreaFix is off for you");
			return;
		}
		{
			String password = FtnTools.getOptionString(link,
					LinkOption.STRING_FILEFIX_PWD);
			if ("".equals(password)) {
				password = link.getPaketPassword();

			}
			if (password.equals(fmsg.getSubject())) {
				FtnTools.writeReply(fmsg, "Access denied", "Wrong password");
				return;
			}
		}
		StringBuilder reply = new StringBuilder();
		for (String line : fmsg.getText().split("\n")) {
			line = line.toLowerCase();
			if (help.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "FileFix help", help());
			} else if (list.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "FileFix list", list(link));
			} else if (query.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "FileFix query", query(link));
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
				+ "%LIST - list of avalible fileareas\n"
				+ "%QUERY - list of subscribed fileareas\n"
				+ "+file.area - subscribe echo.area\n"
				+ "-file.area - unsibscribe echo.area";

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
		sb.append("Legend: * - subscribed\n\n========== List of fileareas ==========\n");
		String[] groups = FtnTools.getOptionStringArray(link,
				LinkOption.SARRAY_LINK_GROUPS);
		List<Filearea> areas = ORMManager.INSTANSE.getFileareaDAO()
				.getOrderAnd("name", true);
		for (Filearea area : areas) {
			boolean denied = true;
			if (!"".equals(area.getGroup())) {
				for (String group : groups) {
					if (area.getGroup().equals(group)) {
						denied = false;
						break;
					}
				}
			} else {
				denied = false;
			}
			if (denied) {
				continue;
			}
			FileSubscription sub = ORMManager.INSTANSE.getFileSubscriptionDAO()
					.getFirstAnd("filearea_id", "=", area.getId(), "link_id",
							"=", link.getId());

			if (sub != null) {
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
		sb.append("========== List of fileareas ==========\n");
		return sb.toString();

	}

	/**
	 * Отправляем %QUERY
	 * 
	 * @param link
	 * @return
	 * @throws SQLException
	 */
	private String query(Link link) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("========== List of subscribed fileareas ==========\n");
		GenericRawResults<String[]> echoes = ORMManager.INSTANSE
				.getFileareaDAO()
				.getRaw(String
						.format("SELECT a.name,a.description FROM filesubscription s"
								+ " RIGHT JOIN filearea a on (a.id=s.filearea_id)"
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
		sb.append("========== List of subscribed fileareas ==========\n");
		return sb.toString();

	}

	private String add(Link link, String area) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String like = area.replace("*", "%");
		String[] grps = FtnTools.getOptionStringArray(link,
				LinkOption.SARRAY_LINK_GROUPS);
		List<Filearea> areas = ORMManager.INSTANSE.getFileareaDAO().getAnd(
				"name", "~", like);
		if (areas.isEmpty()) {
			sb.append(area + " not found");
		} else {
			for (Filearea earea : areas) {
				sb.append(earea.getName());
				FileSubscription sub = ORMManager.INSTANSE
						.getFileSubscriptionDAO().getFirstAnd("filearea_id",
								"=", earea.getId(), "link_id", "=",
								link.getId());
				if (sub != null) {
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
						sub = new FileSubscription();
						sub.setArea(earea);
						sub.setLink(link);
						ORMManager.INSTANSE.getFileSubscriptionDAO().save(sub);
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
		List<Filearea> areas = ORMManager.INSTANSE.getFileareaDAO().getAnd(
				"name", "~", like);
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Filearea earea : areas) {
				sb.append(earea.getName());
				FileSubscription sub = ORMManager.INSTANSE
						.getFileSubscriptionDAO().getFirstAnd("filearea_id",
								"=", earea.getId(), "link_id", "=",
								link.getId());
				if (sub == null) {
					sb.append(" is not subscribed");
				} else {
					ORMManager.INSTANSE.getFileSubscriptionDAO().delete(
							"link_id", "=", link, "filearea_id", "=", earea);
					sb.append(" unsubscribed");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

}
