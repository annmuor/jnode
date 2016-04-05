package org.jnode.rest.auth;

public interface PwdProvider {
    char[] getPwd(String submittedUsername);
}
