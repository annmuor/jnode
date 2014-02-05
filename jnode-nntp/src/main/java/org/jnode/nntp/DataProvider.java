package org.jnode.nntp;

import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Collection;

public interface DataProvider {
    NewsGroup newsGroup(String groupName);
    Collection<NewsGroup> newsGroups();
    Collection<NewsMessage> messagesByGroupName(String groupName);


    Collection<NewsMessage> messagesByIdRange(String fromId, String toId, long groupId);

    NewsMessage messageById(String id);
    NewsMessage messageByMessageId(String messageId);
}
