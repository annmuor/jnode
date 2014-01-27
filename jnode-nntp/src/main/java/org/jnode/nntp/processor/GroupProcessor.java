package org.jnode.nntp.processor;

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

        response.add(
                new StringBuilder()
                        .append("221")
                        .append(" ")
                        .append(group.getNumberOfArticles())
                        .append(" ")
                        .append(group.getReportedLowWatermark())
                        .append(" ")
                        .append(group.getReportedHighWatermark())
                        .toString());
        response.add(NntpResponse.END_OF_RESPONSE);

        return response;
    }
}
