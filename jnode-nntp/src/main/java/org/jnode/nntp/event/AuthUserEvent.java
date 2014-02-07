package org.jnode.nntp.event;

import jnode.event.IEvent;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.model.Auth;

public class AuthUserEvent implements IEvent {

    private Auth auth;

    public AuthUserEvent(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }

    @Override
    public String getEvent() {
        return StringUtils.EMPTY;
    }
}
