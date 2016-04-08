package org.jnode.rest.auth;

public interface PwdProvider {
    boolean isAuth(String token);
}
