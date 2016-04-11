package org.jnode.rest;

import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.logger.Logger;
import jnode.orm.ORMManager;
import jnode.robot.AbstractRobot;
import org.jnode.rest.core.CryptoUtils;
import org.jnode.rest.db.RestUser;

import java.text.MessageFormat;
import java.util.regex.Pattern;

public class UserRobot extends AbstractRobot {

    private static final Logger LOGGER = Logger
            .getLogger(UserRobot.class);

    private static final Pattern TOKEN = Pattern.compile("^%TOKEN$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public void execute(FtnMessage fmsg) throws Exception {
        Link link = getAndCheckLink(fmsg);
        if (link == null) {
            return;
        }

        LOGGER.l5(String.format("process message [%s]", fmsg.getText()));

        for (String line : fmsg.getText().split("\n")) {
            line = line.toLowerCase();

            if (HELP.matcher(line).matches()) {
                FtnTools.writeReply(fmsg,
                        MessageFormat.format("{0} help", getRobotName()),
                        help());
            } else if (TOKEN.matcher(line).matches()) {
                FtnTools.writeReply(fmsg,
                        MessageFormat.format("{0} token", getRobotName()),
                        token(link));
            }

        }

    }

    private String token(Link link) {
        final String pwd = CryptoUtils.randomToken();


        RestUser existed = ORMManager.get(RestUser.class).getFirstAnd("link_id", "=", link.getId());
        if (existed != null){
            existed.setToken(CryptoUtils.sha256(pwd));
            ORMManager.get(RestUser.class).update(existed);
        } else {
            RestUser restUser = new RestUser();
            restUser.setLink(link);
            restUser.setToken(CryptoUtils.sha256(pwd));
            ORMManager.get(RestUser.class).save(restUser);
        }

        return pwd;
    }

    @Override
    protected boolean isEnabled(Link link) {
        return link != null;
    }

    @Override
    protected String getPasswordOption() {
        return "";
    }

    @Override
    protected String help() {
        return "Available commands:\n" + "%HELP - this message\n"
                + "%TOKEN - sent api token\n";
    }
    @Override
    protected String getRobotName() {
        return "RestApiFix";
    }


}
