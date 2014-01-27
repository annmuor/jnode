package org.jnode.nntp.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "news_group")
public class NewsGroup extends NntpEntity {

    @DatabaseField(columnName = "id", index = true, generatedId = true)
    private Long id;

    @DatabaseField(columnName = "unique_prefix")
    private long groupUniquePrefix;

    @DatabaseField(columnName = "name", index = true)
    private String name;

    @DatabaseField(columnName = "number_of_articles")
    private Long numberOfArticles;

    @DatabaseField(columnName = "reported_low_watermark")
    private Long reportedLowWatermark;

    @DatabaseField(columnName = "reported_high_watermark")
    private Long reportedHighWatermark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
