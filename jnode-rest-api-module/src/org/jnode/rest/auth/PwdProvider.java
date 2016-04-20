package org.jnode.rest.auth;

import jnode.logger.Logger;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.RestUserProxy;

import java.util.Date;


@Named("pwdProvider")
@Singleton
public class PwdProvider {

    private static final Logger LOGGER = Logger.getLogger(PwdProvider.class);

    @Inject
    @Named("restUserProxy")
    private RestUserProxy restUserProxy;

    public RestUser isAuth(String token) {

        RestUser user = restUserProxy.findByTokenHash(token);

        LOGGER.l5(String.format("for token %s get %s", token, user));

        if (user == null) {
            return null;
        }

        user.setLastLogin(new Date());
        restUserProxy.update(user);

        return user;
    }

    public void setRestUserProxy(RestUserProxy restUserProxy) {
        this.restUserProxy = restUserProxy;
    }

}
