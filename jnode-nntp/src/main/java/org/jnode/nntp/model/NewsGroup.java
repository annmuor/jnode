package org.jnode.nntp.model;

public class NewsGroup extends NntpEntity {

    private long groupUniquePrefix;
    private String name;
    private Long numberOfArticles;
    private Long reportedLowWatermark;
    private Long reportedHighWatermark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getGroupUniquePrefix() {
        return groupUniquePrefix;
    }

    public void setGroupUniquePrefix(long groupUniquePrefix) {
        this.groupUniquePrefix = groupUniquePrefix;
    }

    public Long getNumberOfArticles() {
        return numberOfArticles;
    }

    public void setNumberOfArticles(Long numberOfArticles) {
        this.numberOfArticles = numberOfArticles;
    }

    public Long getReportedLowWatermark() {
        return reportedLowWatermark;
    }

    public void setReportedLowWatermark(Long reportedLowWatermark) {
        this.reportedLowWatermark = reportedLowWatermark;
    }

    public Long getReportedHighWatermark() {
        return reportedHighWatermark;
    }

    public void setReportedHighWatermark(Long reportedHighWatermark) {
        this.reportedHighWatermark = reportedHighWatermark;
    }
}
