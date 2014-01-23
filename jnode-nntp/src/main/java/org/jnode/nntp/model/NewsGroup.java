package org.jnode.nntp.model;

public class NewsGroup extends NntpEntity {
    private String name;
    private Long numberOfArticles;
    private Long reportedLowWaterMark;
    private Long reportedHighWaterMark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
