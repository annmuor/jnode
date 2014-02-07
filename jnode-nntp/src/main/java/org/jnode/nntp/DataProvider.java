package org.jnode.nntp;

import jnode.dto.Link;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Collection;

public interface DataProvider {
    NewsGroup newsGroup(String groupName, Auth auth);
    Collection<NewsGroup> newsGroups(Auth auth);

    Collection<NewsMessage> messagesByIdRange(String fromId, String toId, long groupId, Auth auth);

    NewsMessage messageById(String id, Long groupId);
    NewsMessage messageByMessageId(String messageId);

    NewsGroup netmail(Auth auth);

    Link link(Auth auth, String pass);
}
