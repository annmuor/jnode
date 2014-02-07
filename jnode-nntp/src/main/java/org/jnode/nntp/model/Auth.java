package org.jnode.nntp.model;

public class Auth {
    private String user;
    private String address;
    private String ftnAddress;

    public Auth(String user, String address, String ftnAddress) {
        this.user = user;
        this.address = address;
        this.ftnAddress = ftnAddress;
    }

    public String getFtnAddress() {
        return ftnAddress;
    }

    public String getUser() {
        return user;
    }

    public String getAddress() {
        return address;
    }
}
