package org.jnode.rest.auth;

import jnode.orm.ORMManager;
import org.jnode.rest.db.RestUser;

public class PwdProvider {

    char[] getPwd(String submittedUsername) {


        RestUser user = ORMManager.get(RestUser.class)
                .getFirstAnd("username", "=", submittedUsername);

        if (user == null) {
            return null;
        }

        return user.getPassword().toCharArray();
    }
}
