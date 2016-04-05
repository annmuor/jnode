package org.jnode.rest.auth;

import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.orm.ORMManager;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Named;


@Named("prod-pwdProvider")
public class PwdProviderImpl implements PwdProvider{

    private static final Logger LOGGER = Logger.getLogger(PwdProviderImpl.class);

    public char[] getPwd(String submittedUsername) {


        RestUser user = ORMManager.get(RestUser.class)
                .getFirstAnd("username", "=", submittedUsername);

        LOGGER.l5(String.format("for username %s get %s", submittedUsername, user));

        if (user == null) {
            return null;
        }

        return FtnTools.md5(user.getPassword()).toCharArray();
    }
}
