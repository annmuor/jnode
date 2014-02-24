package org.jnode.nntp.processor;

import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NewsMessage;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class HeadProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    @Override
    public Collection<String> process(Collection<String> params, Long selectedGroupId, Long selectedArticleId, Auth auth) {

        String id;
        if (params.isEmpty()) {
            id = Long.toString(selectedArticleId);
        } else {
            id = params.iterator().next();
        }

        // try to find message by id
        NewsMessage message = dataProvider.messageById(id, selectedGroupId);
        if (message == null) {
            // try to find message by message id
            message = dataProvider.messageByMessageId(id);
            if (message == null) {
                // message not found
                return responseCantFind();
            } else {
                return responseHeadByMessageId(message);
            }
        } else {
            return responseHeadById(message);
        }
    }

    private Collection<String> responseHeadByMessageId(NewsMessage message) {
        Collection<String> response = new LinkedList<>();

        String responseCode = NntpResponse.Head.HEADERS_FOLLOW;
        responseCode = StringUtils.replace(responseCode, "{message-id}", message.getMessageId());
        responseCode = StringUtils.replace(responseCode, "{n}", "0");

        response.add(responseCode);
        response.add("Path: " + message.getPath());
        response.add("From: " + message.getFrom());
        response.add("Newsgroup: " + message.getGroupName());
        response.add("Subject: " + message.getSubject());
        response.add("Date: " + message.getCreatedDate());
        response.add("Message-ID: " + message.getMessageId());
        response.add(NntpResponse.END);

        return response;
    }

    private Collection<String> responseCantFind() {
        Collection<String> response = new LinkedList<>();
        response.add(NntpResponse.Head.NO_ARTICLE_WITH_THAT_NUMBER);
        return response;
    }

    private Collection<String> responseHeadById(NewsMessage message) {
        Collection<String> response = new LinkedList<>();

        String responseCode = NntpResponse.Head.HEADERS_FOLLOW;
        responseCode = StringUtils.replace(responseCode, "{n}", Long.toString(message.getId()));
        responseCode = StringUtils.replace(responseCode, "{message-id}", message.getMessageId());

        response.add(responseCode);
        response.add("Path: " + message.getPath());
        response.add("From: " + message.getFrom());
        response.add("Newsgroup: " + message.getGroupName());
        response.add("Subject: " + message.getSubject());
        response.add("Date: " + message.getCreatedDate());
        response.add("Message-ID: " + message.getMessageId());
        response.add(NntpResponse.END);

        return response;
    }

}
