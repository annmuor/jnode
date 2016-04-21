package org.jnode.rest.fido;

import org.jnode.rest.db.RestUser;

public interface RestUserProxy {
    RestUser findByGuestLogin(String guestLogin);
    RestUser findByTokenHash(String tokenHash);
    RestUser findByLinkId(Long linkId);
    void save(RestUser restUser);
    void update(RestUser restUser);
}
