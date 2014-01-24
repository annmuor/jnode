package org.jnode.nntp.processor;

import org.jnode.nntp.Processor;

import java.util.Arrays;
import java.util.Collection;

public class XoverProcessor implements Processor {

    private static final String DELIMITER = "\t";

    @Override
    public Collection<String> process(Collection<String> params) {

        StringBuilder response = new StringBuilder();
        response.append("1").append(DELIMITER);
        response.append("article").append(DELIMITER);
        response.append("User <user@xxx.com>").append(DELIMITER);
        response.append("6 Oct 1998 04:38:40 -0500").append(DELIMITER);
        response.append("<111@example.com>").append(DELIMITER);
        response.append("<111@example.net>").append(DELIMITER);
        response.append("1").append(DELIMITER);
        response.append("1").append(DELIMITER);
        return Arrays.asList(response.toString());
    }
}
