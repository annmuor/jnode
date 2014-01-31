package org.jnode.nntp.processor;

import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class ArticleProcessor implements Processor {
    @Override
    public Collection<String> process(Collection<String> params) {
        Collection<String> response = new LinkedList<>();

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

        return response;
    }
}
