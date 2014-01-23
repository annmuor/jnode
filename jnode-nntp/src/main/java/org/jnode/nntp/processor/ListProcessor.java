package org.jnode.nntp.processor;

import org.jnode.nntp.DataProvider;
import org.jnode.nntp.FakeDataProvider;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;

public class ListProcessor implements Processor {

    private DataProvider dataProvider = new FakeDataProvider();

    @Override
    public String process(Collection<String> params) {

        StringBuilder response = new StringBuilder();
        response.append(NntpResponse.List.LIST_OF_NEWSGROUPS).append("\n");

        for (NewsGroup newsGroup : dataProvider.newsGroups()) {
            response.append(newsGroup.getName()).append("\n");
        }

        response.append(NntpResponse.END_OF_RESPONSE).append("\n");

        return response.toString();
    }
}
