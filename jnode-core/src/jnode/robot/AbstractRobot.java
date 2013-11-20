package jnode.robot;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.orm.ORMManager;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public abstract class AbstractRobot implements IRobot {
    protected static final String YOU_ARE_NOT_IN_LINKS_OF_ORIGIN = "You are not in links of origin";
    protected static final String ACCESS_DENIED = "Access denied";
    protected static final String YOU_ARE_NOT_WELCOME = "You are not welcome";
    protected static final String WRONG_PASSWORD = "Wrong password";
    protected static final String SORRY_0_IS_OFF_FOR_YOU = "Sorry, {0} is off for you";

    protected static final Pattern HELP = Pattern.compile("^%HELP$",
            Pattern.CASE_INSENSITIVE);
    protected static final String UNKNOWN_COMMAND_0 = "Unknown command {0}\n";

    protected abstract String getRobotName();

    protected abstract boolean isEnabled(Link link);

    protected abstract String getPasswordOption();

    protected Link getAndCheckLink(FtnMessage fmsg){
        Link link;
        {
            List<Link> links = ORMManager.INSTANSE.getLinkDAO().getAnd(
                    "ftn_address", "=", fmsg.getFromAddr().toString());
            if (links.isEmpty()) {
                FtnTools.writeReply(fmsg, ACCESS_DENIED,
                        YOU_ARE_NOT_IN_LINKS_OF_ORIGIN);
                return null;
            }
            link = links.get(0);
        }
        if (!isEnabled(link)) {
            FtnTools.writeReply(fmsg, YOU_ARE_NOT_WELCOME,
                    MessageFormat.format(SORRY_0_IS_OFF_FOR_YOU, getRobotName()) );
            return null;
        }
        {
            String password = getPassword(link);
            if (!password.equals(fmsg.getSubject())) {
                FtnTools.writeReply(fmsg, ACCESS_DENIED, WRONG_PASSWORD);
                return null;
            }
        }
        return link;
    }

    protected String getPassword(Link link) {
        if (link == null){
            return "";
        }
        String password = FtnTools.getOptionString(link,
                getPasswordOption());
        if ("".equals(password)) {
            password = link.getPaketPassword();
        }
        return password;
    }

    protected abstract String help();

}
