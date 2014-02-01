package org.jnode.nntp.processor;

import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NewsMessage;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class ArticleProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    @Override
    public Collection<String> process(Collection<String> params) {

                                                                                                // todo fix params.iterator().next()
        String id = params.iterator().next();

        NewsMessage message = dataProvider.messageById(id);


        Collection<String> response = new LinkedList<>();

        response.add(NntpResponse.Article.OK);
        response.add(Long.toString(message.getId()) + " " + Long.toString(message.getId()));    // todo Message-ID
        response.add("Path: example.com!not-for-mail");                                         // todo Path
        response.add("From: " + message.getFrom());
        response.add("Newsgroup: " + message.getGroupName());
        response.add("Subject: " + message.getSubject());
        response.add("Date: " + message.getCreatedDate());
        response.add("Message-ID: " + Long.toString(message.getId()));                          // todo Message-ID
        response.add(StringUtils.EMPTY);
        response.add(message.getBody());
        response.add(NntpResponse.END_OF_RESPONSE);

/*
        response.add("220");
        response.add("10100 <10104@xxx.com>");
        response.add("Path: example.com!not-for-mail");
        response.add("From: User <user@example.com>");
        response.add("Newsgroups: group1");
        response.add("Subject: xxx4.");
        response.add("Date: 6 Oct 2000 04:38:40");
        response.add("Organization: An Example Net, Uncertain, Texas");
        response.add("Message-ID: <10104@xxx.com>");
        response.add("");
        response.add("body");
        response.add(NntpResponse.END_OF_RESPONSE);
*/

        return response;
    }
}
