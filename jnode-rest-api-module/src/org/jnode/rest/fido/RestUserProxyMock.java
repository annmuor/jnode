package org.jnode.rest.fido;

import jnode.logger.Logger;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

import java.util.ArrayList;
import java.util.List;

@Named("mock-restUserProxy")
@Singleton
public class RestUserProxyMock implements  RestUserProxy{

    private static final Logger LOGGER = Logger.getLogger(RestUserProxyMock.class);

    private final List<RestUser> data = new ArrayList<>();

    public RestUserProxyMock() {
    }

    @Override
    public RestUser findByGuestLogin(String guestLogin) {

        if(guestLogin == null){
            return null;
        }

        for(RestUser restUser : data){
            if(guestLogin.equals(restUser.getGuestLogin())){
                return restUser;
            }
        }

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
