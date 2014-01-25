package jnode.robot;

import jnode.core.ConcurrentDateFormatAccess;
import jnode.dto.Jscript;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Schedule;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.jscript.JscriptExecutor;
import jnode.orm.ORMManager;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ScriptFix extends AbstractRobot {

	private static final Pattern LIST = Pattern.compile("^%LIST$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern RUN = Pattern.compile("^%RUN (\\d+)$",
			Pattern.CASE_INSENSITIVE);

	private static final ConcurrentDateFormatAccess format = new ConcurrentDateFormatAccess(
			"dd.MM.yyyy HH:mm");

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		Link link = getAndCheckLink(fmsg);
		if (link == null) {
			return;
		}

		for (String line : fmsg.getText().split("\n")) {
			line = line.toLowerCase();

			if (HELP.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} help", getRobotName()),
						help());
			} else if (LIST.matcher(line).matches()) {
				FtnTools.writeReply(fmsg,
						MessageFormat.format("{0} list", getRobotName()),
						list());
			} else {
				Matcher m = RUN.matcher(line);
				if (m.matches()) {
					long id = Long.valueOf(m.group(1));
					FtnTools.writeReply(fmsg, MessageFormat.format(
							"{0} run script {1}", getRobotName(), id),
							runScript(id));
				}
			}

		}
	}

	private String runScript(long id) {
		String errMessage = JscriptExecutor.executeScript(id);
		return errMessage != null ? errMessage : MessageFormat.format(
				"script {0} executed successfully", id);
	}

	@Override
	protected String getRobotName() {
		return "ScriptFix";
	}

	@Override
	protected boolean isEnabled(Link link) {
		return link != null
				&& FtnTools.getOptionBooleanDefFalse(link,
						LinkOption.BOOLEAN_SCRIPTFIX);
	}

	@Override
	protected String getPasswordOption() {
		return LinkOption.STRING_SCRIPTFIX_PWD;
	}

	protected String help() {
		return "Available commands:\n" + "%HELP - this message\n"
				+ "%LIST - list of all scripts\n"
				+ "%RUN scriptId - force run script";
	}

	private String list() throws SQLException {

		StringBuilder sb = new StringBuilder();
		sb.append("============== List of all jscripts ===============\n");
		sb.append("| id  |                   content                  |\n");
		sb.append("|-----|--------------------------------------------|\n");
		for (Jscript js : ORMManager.get(Jscript.class).getOrderAnd("id", true)) {
			String code = js.getContent();
			boolean first = true;
			for (int i = 0; i < code.length(); i += 42) {
				int endIndex = (code.length() > i + 42) ? i + 42 : code
						.length();
				String sub = code.substring(i, endIndex);
				String id = (first) ? String.format("%05d", js.getId())
						: "     ";
				for (int j = 42; j > sub.length(); j--) {
					sub += " ";
				}
				sb.append("|" + id + "| " + sub + " |\n");
				first = false;
			}
			sb.append("|-----|--------------scheduled-at------------------|\n");
			for (Schedule s : ORMManager.get(Schedule.class).getAnd(
					"jscript_id", "=", js)) {
				String fmt = String.format(
						"|%05d|      %s AT %02d LAST %s",
						s.getId(),
						s.getType().name(),
						s.getDetails(),
						(s.getLastRunDate() != null) ? format.format(s
								.getLastRunDate()) : "NEVER");
				for (int j = 53; j > fmt.length(); j--) {
					fmt += " ";
				}
				fmt += "|\n";
				sb.append(fmt);
			}
			sb.append("|-----|--------------------------------------------|\n");

		}
		sb.append("============== List of all jscripts ================\n");
		return sb.toString();

	}
}
