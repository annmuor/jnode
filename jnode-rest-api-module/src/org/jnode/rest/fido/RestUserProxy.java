package org.jnode.rest.fido;

import org.jnode.rest.db.RestUser;

public interface RestUserProxy {
    RestUser findByGuestLogin(String guestLogin);
    RestUser findByUserCredentials(String userLogin, String userPwd);
    void save(RestUser restUser);
    void update(RestUser restUser);
}
