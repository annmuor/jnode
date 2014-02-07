package org.jnode.nntp.processor;

import org.jnode.nntp.model.Auth;

public class BaseProcessor {

    public boolean isAuthorized(Auth auth) {
        return auth.getFtnAddress() != null;
    }
}
