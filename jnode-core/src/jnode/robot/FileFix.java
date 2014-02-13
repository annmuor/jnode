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
public class FileFix extends AbstractRobot {
	private static final Pattern LIST = Pattern.compile("^%LIST$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern QUERY = Pattern.compile("^%QUERY$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern ADD = Pattern.compile("^%?\\+?(\\S+)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern REM = Pattern.compile("^%?\\-(\\S+)$",
			Pattern.CASE_INSENSITIVE);

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
			}
		}
		if (reply.length() > 0) {
			FtnTools.writeReply(fmsg,
					MessageFormat.format("{0} reply", getRobotName()),
					reply.toString());
		}
	}

	/**
	 * Отправляем %HELP
	 * 
	 * @return
	 */
	protected String help() {
		return "Available commands:\n" + "%HELP - this message\n"
				+ "%ASLINK ftn_address - proccess command as other link ( not the origin )\n"
				+ "%LIST - list of availible fileareas\n"
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
		List<Filearea> areas = ORMManager.get(Filearea.class).getOrderAnd(
				"name", true);
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
			FileSubscription sub = ORMManager.get(FileSubscription.class)
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
		GenericRawResults<String[]> echoes = ORMManager
				.get(Filearea.class)
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
		List<Filearea> areas = ORMManager.get(Filearea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			sb.append(area + " not found");
		} else {
			for (Filearea earea : areas) {
				sb.append(earea.getName());
				FileSubscription sub = ORMManager.get(FileSubscription.class)
						.getFirstAnd("filearea_id", "=", earea.getId(),
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
						sub = new FileSubscription();
						sub.setArea(earea);
						sub.setLink(link);
						ORMManager.get(FileSubscription.class).save(sub);
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
		List<Filearea> areas = ORMManager.get(Filearea.class).getAnd("name",
				"~", like);
		if (areas.isEmpty()) {
			sb.append(area);
			sb.append(" not found");
		} else {
			for (Filearea earea : areas) {
				sb.append(earea.getName());
				FileSubscription sub = ORMManager.get(FileSubscription.class)
						.getFirstAnd("filearea_id", "=", earea.getId(),
								"link_id", "=", link.getId());
				if (sub == null) {
					sb.append(" is not subscribed");
				} else {
					ORMManager.get(FileSubscription.class).delete("link_id",
							"=", link, "filearea_id", "=", earea);
					sb.append(" unsubscribed");
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	@Override
	protected String getRobotName() {
		return "FileFix";
	}

	@Override
	protected boolean isEnabled(Link link) {
		return link != null
				&& FtnTools.getOptionBooleanDefTrue(link,
						LinkOption.BOOLEAN_FILEFIX);
	}

	@Override
	protected String getPasswordOption() {
		return LinkOption.STRING_FILEFIX_PWD;
	}

}
