package org.jnode.nntp.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "news_message")
public class NewsMessage extends NntpEntity {

    @DatabaseField(columnName = "id", index = true, id = true)
    private Long id;

    @DatabaseField(columnName = "group_name", index = true)
    private String groupName;

    @DatabaseField(columnName = "from")
    private String from;

    @DatabaseField(columnName = "subject")
    private String subject;

    @DatabaseField(columnName = "body")
    private String body;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
