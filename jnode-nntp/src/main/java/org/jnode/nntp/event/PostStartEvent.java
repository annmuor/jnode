package org.jnode.nntp.event;

import jnode.event.IEvent;
import org.apache.commons.lang.StringUtils;

public class PostStartEvent implements IEvent {

    @Override
    public String getEvent() {
        return StringUtils.EMPTY;
    }
}
