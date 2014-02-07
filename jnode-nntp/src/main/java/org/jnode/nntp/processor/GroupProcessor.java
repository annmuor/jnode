package org.jnode.nntp.processor;

import jnode.event.Notifier;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.event.GroupSelectedEvent;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class GroupProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    @Override
    public Collection<String> process(Collection<String> params, Long id, Long selectedArticleId, Auth auth) {

        String groupName = params.iterator().next();
        NewsGroup group = dataProvider.newsGroup(groupName, auth);
        if (group == null) {
            return responseNotFound();
        }

        Notifier.INSTANSE.notify(new GroupSelectedEvent(group));

        return responseGroup(groupName, group);
    }

    private Collection<String> responseNotFound() {
        Collection<String> response = new LinkedList<>();
        response.add(NntpResponse.Group.NO_SUCH_NEWSGROUP);
        return response;
    }

    private Collection<String> responseGroup(String groupName, NewsGroup group) {
        Collection<String> response = new LinkedList<>();

        String resposeCode = NntpResponse.Group.GROUP_SUCCESSFULLY_SELECTED;
        resposeCode = StringUtils.replace(resposeCode, "{number}", Long.toString(group.getNumberOfArticles()));
        resposeCode = StringUtils.replace(resposeCode, "{low}", Long.toString(group.getReportedLowWatermark()));
        resposeCode = StringUtils.replace(resposeCode, "{high}", Long.toString(group.getReportedHighWatermark()));
        resposeCode = StringUtils.replace(resposeCode, "{group}", groupName);

        response.add(resposeCode);

        return response;
    }
}
