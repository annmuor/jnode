package org.jnode.nntp.event;

import jnode.event.IEvent;
import org.apache.commons.lang.StringUtils;

public class ArticleSelectedEvent implements IEvent {

    private Long selectedArticleId;

    public ArticleSelectedEvent(Long selectedArticleId) {
        this.selectedArticleId = selectedArticleId;
    }

    public Long getSelectedArticleId() {
        return selectedArticleId;
    }

    @Override
    public String getEvent() {
        return StringUtils.EMPTY;
    }
}
