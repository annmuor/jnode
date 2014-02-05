package org.jnode.nntp.processor;

import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NewsMessage;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class HeadProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    private static final String DELIMITER = "\n";

    @Override
    public Collection<String> process(Collection<String> params, Long selectedGroupId) {

        // todo refactor params.iterator().next()
        String id = params.iterator().next();

        NewsMessage message = dataProvider.messageById(id);

        Collection<String> response = new LinkedList<>();

        response.add("221 " + message.getId() + message.getMessageId() + "\r");
        response.add("Path: " + message.getPath());
        response.add("From: " + message.getFrom());
        response.add("Newsgroups: " + message.getGroupName());
        response.add("Subject: " + message.getSubject());
        response.add("Date: 6 Oct 2000 04:38:40");
        response.add("Message-ID: " + message.getMessageId());
        response.add(NntpResponse.END_OF_RESPONSE);

        return response;
    }

}
