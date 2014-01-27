package org.jnode.nntp.processor;

import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class ListProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    @Override
    public Collection<String> process(Collection<String> params) {

        Collection<String> response = new LinkedList<>();
        response.add(NntpResponse.List.LIST_OF_NEWSGROUPS);

        for (NewsGroup newsGroup : dataProvider.newsGroups()) {
            response.add(newsGroup.getName());
        }

        response.add(NntpResponse.END_OF_RESPONSE);

        return response;
    }
}
