package org.jnode.nntp.event;

import jnode.event.IEvent;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.model.NewsGroup;

public class GroupSelectedEvent implements IEvent {

    private NewsGroup selectedGroup;

    public GroupSelectedEvent(NewsGroup selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public NewsGroup getSelectedGroup() {
        return selectedGroup;
    }

    @Override
    public String getEvent() {
        return StringUtils.EMPTY;
    }
}
