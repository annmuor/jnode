package org.jnode.nntp.processor;

import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class HeadProcessor implements Processor {

    private static final String DELIMITER = "\n";

    @Override
    public Collection<String> process(Collection<String> params) {

        Collection<String> response = new LinkedList<>();

        response.add("221 1 <111@example.com>");
        response.add("Path: example.com!not-for-mail");
        response.add("From: User <user@example.com>");
        response.add("Newsgroups: name");
        response.add("Subject: lalalala.");
        response.add("Date: 6 Oct 1998 04:38:40");
        response.add("Organization: An Example Net, Uncertain, Texas");
        response.add("Message-ID: <111@example.com>");
        response.add(NntpResponse.END_OF_RESPONSE);

        return response;
    }

}
