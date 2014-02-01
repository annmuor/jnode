package org.jnode.nntp.processor;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.NewsMessage;
import org.jnode.nntp.model.NntpResponse;

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

        Collection<String> response = Lists.newLinkedList();

        response.add("224");

        for (NewsMessage message : messages) {
            StringBuilder builder = new StringBuilder();

            builder.append(Long.toString(message.getId())).append(DELIMITER);
            builder.append(message.getSubject()).append(DELIMITER);
            builder.append(message.getFrom()).append(DELIMITER);
            builder.append(message.getCreatedDate().toString()).append(DELIMITER);
            builder.append(Long.toString(message.getId())).append(DELIMITER);
            builder.append(1).append(DELIMITER); // todo References header content
            builder.append(1).append(DELIMITER); // todo :bytes metadata item
            builder.append(1).append(DELIMITER); // todo :lines metadata item
            builder.append(DELIMITER);

            response.add(builder.toString());
        }

        response.add(NntpResponse.END_OF_RESPONSE);

        return response;
    }
}
