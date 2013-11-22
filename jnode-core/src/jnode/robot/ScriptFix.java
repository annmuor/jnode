package jnode.robot;

import com.j256.ormlite.dao.GenericRawResults;
import jnode.dto.Link;
import jnode.dto.LinkOption;
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
    private static final Pattern RUN = Pattern.compile(
            "^%RUN (\\d+)$", Pattern.CASE_INSENSITIVE);

    @Override
    public void execute(FtnMessage fmsg) throws Exception {
        Link link = getAndCheckLink(fmsg);
        if (link == null){
            return;
        }

        for (String line : fmsg.getText().split("\n")) {
            line = line.toLowerCase();

            if (HELP.matcher(line).matches()) {
                FtnTools.writeReply(fmsg, MessageFormat.format("{0} help", getRobotName()), help());
            } else if (LIST.matcher(line).matches())  {
                FtnTools.writeReply(fmsg, MessageFormat.format("{0} list", getRobotName()), list());
            } else {
                Matcher m = RUN.matcher(line);
                if (m.matches()) {
                    long id = Long.valueOf(m.group(1));
                    FtnTools.writeReply(fmsg, MessageFormat.format("{0} run script {1}", getRobotName(), id), runScript(id));
                }
            }

        }
    }

    private String runScript(long id){
        String errMessage = JscriptExecutor.executeScript(id);
        return errMessage != null ? errMessage : MessageFormat.format("script {0} executed successfully", id);
    }

    @Override
    protected String getRobotName() {
        return "ScriptFix";
    }

    @Override
    protected boolean isEnabled(Link link) {
        return link != null && FtnTools.getOptionBooleanDefFalse(link, LinkOption.BOOLEAN_SCRIPTFIX);
    }

    @Override
    protected String getPasswordOption() {
        return LinkOption.STRING_SCRIPTFIX_PWD;
    }

    protected String help() {
        return "Available commands:\n" +
                "%HELP - this message\n" +
                "%LIST - list of all scripts\n" +
                "%RUN scriptId - force run script";
    }

    private String list() throws SQLException {

        final class Answer{
            private final StringBuilder sb;

            private Answer(StringBuilder sb) {
                this.sb = sb;
            }

            private void print (String header, String item){
                sb.append(header);
                sb.append(": ");
                sb.append(String.valueOf(item));
                sb.append('\n');
            }

            private void parse(String[] tokens){
                print("script id", tokens[0]);
                print("schedule id", tokens[1]);
                print("type", tokens[2]);
                print("details", tokens[3]);
                print("last run date", tokens[4]);
                print("content", tokens[5]);
            }
        }

        StringBuilder sb = new StringBuilder();
        Answer answer = new Answer(sb);
        sb.append("========== List of all jscripts ==========\n");
        GenericRawResults<String[]> items = ORMManager.INSTANSE
                .getJscriptDAO()
                .getRaw("SELECT J.ID, S.ID AS S_ID, S.TYPE, S.DETAILS, " +
                        " S.LASTRUNDATE, J.CONTENT FROM JSCRIPTS J LEFT JOIN  SCHEDULE S "+
                        " ON (J.ID = S.JSCRIPT_ID) ORDER BY ID;");
        for (String[] tokens : items.getResults()) {
            answer.parse(tokens);
            sb.append("--------------------------\n");
        }
        sb.append("========== List of all jscripts ==========\n");
        return sb.toString();

    }


}
