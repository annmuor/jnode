package org.jnode.rest.fido.mock;

import jnode.logger.Logger;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.RestUserProxy;

import java.util.ArrayList;
import java.util.List;

@Named("mock-restUserProxy")
@Singleton
public class RestUserProxyMock implements RestUserProxy {

    private static final Logger LOGGER = Logger.getLogger(RestUserProxyMock.class);

    private long seq = 0L;

    private final List<RestUser> data = new ArrayList<>();

    @Override
    public synchronized RestUser findByGuestLogin(String guestLogin) {

        if(guestLogin == null){
            return null;
        }

        for(RestUser restUser : data){
            if(guestLogin.equals(restUser.getGuestLogin())){
                LOGGER.l5("find " + restUser);
                return restUser;
            }
        }

        LOGGER.l5("not found " + guestLogin);
        return null;
    }

    @Override
    public synchronized RestUser findByTokenHash(String tokenHash) {
        if (tokenHash == null){
            return null;
        }

        for(RestUser restUser : data){
            if (tokenHash.equals(restUser.getToken())){
                return restUser;
            }
        }

        return null;
    }

    @Override
    public synchronized RestUser findByLinkId(Long linkId) {
        if (linkId == null){
            return null;
        }

        for(RestUser restUser : data){
            if (restUser.getLink() != null && linkId.equals(restUser.getLink().getId())){
                return restUser;
            }
        }

        return null;
    }

    @Override
    public synchronized void save(RestUser restUser) {
        restUser.setId(++seq);
        data.add(restUser);
        LOGGER.l5("save " + restUser);
    }

    @Override
    public synchronized void update(RestUser restUser) {
        int i = data.indexOf(restUser);
        if (i < 0){
            return;
        }
        data.set(i, restUser);
        LOGGER.l5("update " + restUser);
    }
}
