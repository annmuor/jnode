package org.jnode.nntp.processor;

import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class GroupProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    @Override
    public Collection<String> process(Collection<String> params) {

        String groupName = params.iterator().next();
        NewsGroup group = dataProvider.newsGroup(groupName);

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
