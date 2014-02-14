package org.jnode.nntp;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.Link;
import jnode.dto.Netmail;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Collection;

public interface DataProvider {
    Echoarea echoarea(String echoareaName);
    NewsGroup newsGroup(String groupName, Auth auth);
    Collection<NewsGroup> newsGroups(Auth auth);

    Collection<NewsMessage> messagesByIdRange(String fromId, String toId, long groupId, Auth auth);

    NewsMessage messageById(String id, Long groupId);
    NewsMessage messageByMessageId(String messageId);

    NewsGroup netmail(Auth auth);

    Link link(Auth auth, String pass);

    void post(Netmail netmail);
    void post(Auth auth, Echomail netmail);
}
