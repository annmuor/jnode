package org.jnode.rest.auth;

import org.jnode.rest.db.RestUser;

public interface PwdProvider {
    RestUser isAuth(String token);
}
