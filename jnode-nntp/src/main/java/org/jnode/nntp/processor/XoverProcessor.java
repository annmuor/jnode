package org.jnode.nntp.processor;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NewsMessage;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;

public class XoverProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    private static final String DELIMITER = "\t";

    @Override
    public Collection<String> process(Collection<String> params, Long selectedGroupId, Long selectedArticleId, Auth auth) {

        String range = params.iterator().next();

        Collection<NewsMessage> messages = Lists.newLinkedList();

        if (range.contains("-")) {
            messagesByRange(selectedGroupId, range, messages, auth);
        } else {
           messages.add(dataProvider.messageById(range, selectedGroupId));
        }

        Collection<String> response = Lists.newLinkedList();

        response.add(NntpResponse.Xover.OVERVIEW_INFORMATION_FOLLOWS);

        for (NewsMessage message : messages) {
            StringBuilder builder = new StringBuilder();

            builder.append(Long.toString(message.getId())).append(DELIMITER);
            builder.append(message.getSubject()).append(DELIMITER);
            builder.append(message.getFrom()).append(DELIMITER);
            builder.append(message.getCreatedDate().toString()).append(DELIMITER);
            builder.append(message.getMessageId()).append(DELIMITER);
            builder.append(1).append(DELIMITER); // todo References header content
            builder.append(1).append(DELIMITER); // todo :bytes metadata item
            builder.append(1).append(DELIMITER); // todo :lines metadata item
            builder.append(DELIMITER);

            response.add(builder.toString());
        }

        response.add(NntpResponse.END);

        return response;
    }

    private void messagesByRange(Long selectedGroupId, String range, Collection<NewsMessage> messages, Auth auth) {
        String[] parts = StringUtils.split(range, "-");
        switch (parts.length) {
            case 1:
                messages.add(dataProvider.messageById(parts[0], selectedGroupId));
                break;
            case 2:
                messages.addAll(dataProvider.messagesByIdRange(parts[0], parts[1], selectedGroupId, auth));
                break;
            default:
        }
    }
}
