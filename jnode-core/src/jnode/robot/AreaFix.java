/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.robot;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.dao.GenericRawResults;

import jnode.dto.*;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.orm.ORMManager;

/**
 * 
 * @author kreon
 * 
 */
public class AreaFix extends AbstractRobot {
	private static final Pattern LIST = Pattern.compile("^%LIST$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern QUERY = Pattern.compile("^%QUERY$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern ADD = Pattern.compile("^%?\\+?(\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern REM = Pattern.compile("^%?\\-(\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern RESCAN = Pattern.compile(
			"^%RESCAN (\\S+) (\\d+)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern ADD_RESCAN = Pattern.compile(
			"^%?\\+?(\\S+) /r=(\\d+)$", Pattern.CASE_INSENSITIVE);

	private static final Pattern AFXPASS = Pattern.compile("^%AFXPASS (\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern PKTPASS = Pattern.compile("^%PKTPASS (\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern IGNOREPKTPWD = Pattern.compile(
			"^%IGNOREPKTPWD (on|off)$", Pattern.CASE_INSENSITIVE);

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		Link link = getAndCheckLink(fmsg);
		if (link == null) {
			return;
		}

		StringBuilder reply = new StringBuilder();
		for (String line : fmsg.getText().split("\n")) {
			line = line.toLowerCase();
			if (HELP.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} help", getRobotName()),
						help());
			} else if (LIST.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} list", getRobotName()),
						list(link));
			} else if (QUERY.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} query", getRobotName()),
						query(link));
			} else {
				Matcher m = REM.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					reply.append(rem(link, area));
					continue;
				}
				m = ADD.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					reply.append(add(link, area));
					continue;
				}
				m = RESCAN.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					int num = Integer.valueOf(m.group(2));
					reply.append(rescan(link, area, num));
					continue;
				}
				m = ADD_RESCAN.matcher(line);
				if (m.matches()) {
					String area = m.group(1);
					int num = Integer.valueOf(m.group(2));
					reply.append(add(link, area));
					reply.append(rescan(link, area, num));
					continue;
				}
				m = AFXPASS.matcher(line);
				if (m.matches()) {
					String newpwd = m.group(1);
					reply.append(afxpass(link, newpwd));
					continue;
				}
				m = PKTPASS.matcher(line);
				if (m.matches()) {
					String newpwd = m.group(1);
					reply.append(pktpass(link, newpwd));
					continue;
				}

				m = IGNOREPKTPWD.matcher(line);
				if (m.matches()) {
					String onoff = m.group(1);
					reply.append(ignorepktpwd(link, onoff));
					continue;
				}

			}
		}
		if (reply.length() > 0) {
			FtnTools.writeReply(fmsg,
					MessageFormat.format("{0} reply", getRobotName()),
					reply.toString());
		}
	}

	private String ignorepktpwd(Link link, String onoff) {
		if (onoff.equals("on")) {
			FtnTools.setOption(link, LinkOption.BOOLEAN_IGNORE_PKTPWD, "true");
			return "We will ignore password in your packets\n";
		} else {
			FtnTools.setOption(link, LinkOption.BOOLEAN_IGNORE_PKTPWD, "false");
			return "We will check password in your packets\n";
		}
	}

	private String pktpass(Link link, String newpwd) {
		if (newpwd.length() > 3 && newpwd.length() < 9) {
			link.setPaketPassword(newpwd);
			ORMManager.get(Link.class).update(link);
			return "Your packet password was changed to " + newpwd + "\n";
		} else {
			return "Your packet password must be between 4 and 8 chars length\n";
		}
	}

	protected String afxpass(Link link, String newpwd) {
		if (newpwd.length() > 3 && newpwd.length() < 17) {
			FtnTools.setOption(link, LinkOption.STRING_AREAFIX_PWD, newpwd);
			return "Your AreaFix password was changed to " + newpwd + "\n";
		} else {
			return "Your AreaFix password must be between 4 and 16 chars length\n";
		}
	}

	/**
	 * Отправляем %HELP
	 * 
	 * @return
	 */
	protected String help() {
		return "Available commands:\n"
				+ "%HELP - this message\n"
				+ "%ASLINK ftn address - proccess command as other link ( not the origin )\n"
				+ "%LIST - list of available areas\n"
				+ "%QUERY - list of subscribed areas\n"
				+ "%AFXPASS password - change areafix password\n"
				+ "%PKTPASS password - change pkt password\n"
				+ "%IGNOREPKTPWD on|off - turn on/off checking pkt passwords in pkts from you\n"
				+ "+echo.area - subscribe echo.area\n"
				+ "-echo.area - unsibscribe echo.area\n"
				+ "+echo.area /r=N - subscribe and rescan N messages\n"
				+ "%RESCAN echo.area N - rescan N messages";

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
		String[] groups = FtnTools.getOptionStringArray(link,
				LinkOption.SARRAY_LINK_GROUPS);
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getOrderAnd(
				"name", true);
		for (Echoarea area : areas) {
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
			Subscription sub = ORMManager.get(Subscription.class).getFirstAnd(
					"echoarea_id", "=", area.getId(), "link_id", "=",
					link.getId());

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
		sb.append("========== List of echoareas ==========\n");
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
		sb.append("========== List of subscribed areas ==========\n");
		GenericRawResults<String[]> echoes = ORMManager
				.get(Echoarea.class)
				.getRaw(String
						.format("SELECT a.name, a.description from echoarea a"
								+ " LEFT JOIN subscription s on (a.id=s.echoarea_id)"
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
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			// check-check: let's make an request
			// TODO: make it :-)
			// Link uplink = ORMManager
			// .get(Link.class)
			// .join(true)
			// .join(LinkOption.class, true, "name", "=",
			// LinkOption.BOOLEAN_FORWARD_AREAFIX, "value", "=",
			// "true").one();
			sb.append(area + " not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				Subscription sub = ORMManager.get(Subscription.class)
						.getFirstAnd("echoarea_id", "=", earea.getId(),
								"link_id", "=", link.getId());
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
						sub = new Subscription();
						sub.setArea(earea);
						sub.setLink(link);
						ORMManager.get(Subscription.class).save(sub);
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
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				Subscription sub = ORMManager.get(Subscription.class)
						.getFirstAnd("echoarea_id", "=", earea.getId(),
								"link_id", "=", link.getId());
				if (sub == null) {
					sb.append(" is not subscribed");
				} else {
					ORMManager.get(Subscription.class).delete("link_id", "=",
							link, "echoarea_id", "=", earea);
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
		List<Echoarea> areas = ORMManager.get(Echoarea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Echoarea earea : areas) {
				sb.append(earea.getName());
				Subscription sub = ORMManager.get(Subscription.class)
						.getFirstAnd("echoarea_id", "=", earea.getId(),
								"link_id", "=", link.getId());
				if (sub == null) {
					sb.append(" is not subscribed");
				} else {
					List<Echomail> mails = ORMManager.get(Echomail.class)
							.getOrderLimitAnd(num, "id", false, "echoarea_id",
									"=", earea);
					for (int i = mails.size() - 1; i >= 0; --i) {
						Echomail mail = mails.get(i);
						ORMManager.get(EchomailAwaiting.class).save(
								new EchomailAwaiting(link, mail));
					}
					sb.append(" rescanned " + mails.size() + " messages");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	@Override
	protected String getRobotName() {
		return "AreaFix";
	}

	@Override
	protected boolean isEnabled(Link link) {
		return link != null
				&& FtnTools.getOptionBooleanDefTrue(link,
						LinkOption.BOOLEAN_AREAFIX);
	}

	@Override
	protected String getPasswordOption() {
		return LinkOption.STRING_AREAFIX_PWD;
	}

}
