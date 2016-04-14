package org.jnode.rest.fido;

import jnode.dto.Link;
import jnode.orm.ORMManager;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

@Named("prod-restUserProxy")
@Singleton
public class RestUserProxyImpl implements  RestUserProxy{
    @Override
    public RestUser findByGuestLogin(String guestLogin) {
        return ORMManager.get(RestUser.class).getFirstAnd(RestUser.GUESTLOGIN_FIELD, "=", guestLogin);
    }

    @Override
    public RestUser findByUserCredentials(String userLogin, String userPwd) {

        Link link = ORMManager.get(Link.class).getFirstAnd("ftn_address", "=", userLogin);

        if (link == null){
            return null;
        }

        if (link.getPaketPassword() != null && link.getPaketPassword().equals(userPwd)){
            return ORMManager.get(RestUser.class).getFirstAnd(RestUser.LINK_ID_FIELD, "=", link.getId());
        }

        return null;
    }

    @Override
    public void save(RestUser restUser) {
        ORMManager.get(RestUser.class).save(restUser);
    }

    @Override
    public void update(RestUser restUser) {
        ORMManager.get(RestUser.class).update(restUser);
    }
}
