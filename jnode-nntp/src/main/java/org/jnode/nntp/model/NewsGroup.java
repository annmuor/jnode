package org.jnode.nntp.model;

public class NewsGroup extends NntpEntity {

    private String name;
    private int numberOfArticles;
    private Long reportedLowWatermark;
    private Long reportedHighWatermark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfArticles() {
        return numberOfArticles;
    }

    public void setNumberOfArticles(int numberOfArticles) {
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
