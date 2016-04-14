package org.jnode.rest.fido;

import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

@Named("mock-restUserProxy")
@Singleton
public class RestUserProxyMock implements  RestUserProxy{
    @Override
    public RestUser findByGuestLogin(String guestLogin) {
        return null;
    }

    @Override
    public RestUser findByUserCredentials(String userLogin, String userPwd) {
        return null;
    }

    @Override
    public void save(RestUser restUser) {

    }

    @Override
    public void update(RestUser restUser) {

    }
}
