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

import jnode.core.ConcurrentDateFormatAccess;
import jnode.dto.Jscript;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Schedule;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.jscript.JScriptConsole;
import jnode.jscript.JscriptExecutor;
import jnode.logger.Logger;
import jnode.orm.ORMManager;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ScriptFix extends AbstractRobot {

    private final Logger logger = Logger
            .getLogger(getClass());

	private static final Pattern LIST = Pattern.compile("^%LIST$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern RUN = Pattern.compile("^%RUN (\\d+)$",
			Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT = Pattern.compile("\\{(.*)?\\}",
            Pattern.DOTALL);

	private static final ConcurrentDateFormatAccess format = new ConcurrentDateFormatAccess(
			"dd.MM.yyyy HH:mm");

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		Link link = getAndCheckLink(fmsg);
		if (link == null) {
			return;
		}

        // если скрипт - то фигарим скрипт
        String scriptContent = extractScript(fmsg.getText());
        if (scriptContent != null){
            processScript(fmsg, scriptContent);
        } else{
            processCommands(fmsg);
        }

	}

    private void processScript(FtnMessage fmsg, String scriptContent) {
        String output = executeScriptWithConsole(scriptContent, false);
        FtnTools.writeReply(fmsg,
                MessageFormat.format("{0} exec script", getRobotName()),
                output != null ? output : "Okay");
    }

    static String executeScriptWithConsole(String scriptContent, boolean force) {
        Bindings bindings = new SimpleBindings();
        final JScriptConsole jScriptConsole = new JScriptConsole();
        bindings.put("console", jScriptConsole);
        String result = JscriptExecutor.executeScript(scriptContent, bindings, force);
        if (result != null){
            jScriptConsole.log(String.format("\n%s", result));
        }
        return jScriptConsole.out();
    }

    private void processCommands(FtnMessage fmsg) throws SQLException {
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
                Long id = extractScriptId(line);
                if (id != null) {
                    FtnTools.writeReply(fmsg, MessageFormat.format(
                                    "{0} run script {1}", getRobotName(), id),
                            runScript(id));
                }
            }

        }
    }

    static Long extractScriptId(String line){
        Matcher m = RUN.matcher(line);
        if (m.matches()) {
            return Long.valueOf(m.group(1));
        }
        return null;
    }

    static String extractScript(String text){
        Matcher m = SCRIPT.matcher(text);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
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
        if (logger.isNeedLog5()){
            if (link == null){
                logger.l5("AHTUNG! NULL link!");
            } else {
                logger.l5(MessageFormat.format("isEnabled - for link {0} scriptfix activity is {1}", link, FtnTools.getOptionBooleanDefFalse(link,
                        LinkOption.BOOLEAN_SCRIPTFIX)));
            }
        }
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
				+ "%ASLINK ftn_address - proccess command as other link ( not the origin )\n"
				+ "%LIST - list of all scripts\n"
				+ "%RUN scriptId - force run script\n"
                + "{multiline script} - execute multiline script";
	}

	private String list() throws SQLException {

		StringBuilder sb = new StringBuilder();
		sb.append("==============  List of all jscripts ===============\n");
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
