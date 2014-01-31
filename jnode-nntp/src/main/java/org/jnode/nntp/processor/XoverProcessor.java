package org.jnode.nntp.processor;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NewsMessage;

import java.util.Collection;

public class XoverProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    private static final String DELIMITER = "\t";

    @Override
    public Collection<String> process(Collection<String> params) {

        // todo fix params.iterator().next()
        String[] parts = StringUtils.split(params.iterator().next(), "-");
        Collection<NewsMessage> messages = Lists.newLinkedList();

        switch (parts.length) {
            case 1:
              //  messages.add(dataProvider.messageById(parts[0])); // todo
                break;
            case 2:
                messages.addAll(dataProvider.messagesByIdRange(parts[0], parts[1]));
                break;
            default:
        }

        String r = "10100\txxx4.\tUser\t6 Oct 2000 04:38:40\t<10104@xxx.com>\t1\t1\t1\t\t";

        Collection<String> response = Lists.newLinkedList();
        response.add("224");
        response.add(r);
        response.add(".");

/*
        response.add("224\r");

        for (NewsMessage message : messages) {
          //  response.add( " + Long.toString(message.getId()));
            response.add("Subject: " + message.getSubject() + "\r");
            response.add("From: " + message.getFrom());
            response.add("Date: " + "6 Oct 2000 04:38:40");
            response.add("Message-ID: " + "<" + Long.toString(message.getId()) + "@xxx.com>" );   // todo Message-ID header content ?
            //response.add(Long.toString(message.getId()));   // todo References header content ?
            //response.add("1");                              // todo :bytes metadata item ?
           // response.add("1");                              // todo :lines metadata item ?
        }

        response.add(NntpResponse.END_OF_RESPONSE);
*/

        return response;
    }
}
