package org.jnode.nntp.model;

public class Auth {
    private Long linkId;
    private String user;
    private String address;
    private String ftnAddress;

    public Auth(String user, String address, String ftnAddress) {
        this.user = user;
        this.address = address;
        this.ftnAddress = ftnAddress;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
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

    public void reset() {
        this.linkId = null;
        this.user = null;
        this.address = null;
        this.ftnAddress = null;
    }
}
