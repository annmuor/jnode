package org.jnode.nntp.model;

public class NewsMessage extends NntpEntity {
    private String nntpGroupCode;
    private String body;

    public String getNntpGroupCode() {
        return nntpGroupCode;
    }

    public void setNntpGroupCode(String nntpGroupCode) {
        this.nntpGroupCode = nntpGroupCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
