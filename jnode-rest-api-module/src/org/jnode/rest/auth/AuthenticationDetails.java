package org.jnode.rest.auth;

public class AuthenticationDetails {
    public String username;

    public char[] password;

    public AuthenticationDetails(final String username, final String password)
    {
        this.username = username;
        this.password = password.toCharArray();
    }
}
