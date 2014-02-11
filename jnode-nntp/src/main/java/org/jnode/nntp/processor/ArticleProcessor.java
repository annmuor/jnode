package org.jnode.nntp.processor;

import jnode.event.Notifier;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.event.ArticleSelectedEvent;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NewsMessage;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;
import java.util.LinkedList;

public class ArticleProcessor implements Processor {

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
                markArticleAsSelected(message);
                return responseArticleByMessageId(message);
            }
        } else {
            return responseArticleById(message);
        }

    }

    private void markArticleAsSelected(NewsMessage message) {
        Notifier.INSTANSE.notify(new ArticleSelectedEvent(message.getId()));
    }

    private Collection<String> responseArticleByMessageId(NewsMessage message) {
        Collection<String> response = new LinkedList<>();

        String responseCode = NntpResponse.Article.ARTICLE_FOLLOWS_1;
        responseCode = StringUtils.replace(responseCode, "{message-id}", message.getMessageId());

        response.add(responseCode);
        response.add("Path: " + message.getPath());
        response.add("From: " + message.getFrom());
        response.add("Newsgroup: " + message.getGroupName());
        response.add("Subject: " + message.getSubject());
        response.add("Date: " + message.getCreatedDate());
        response.add("Message-ID: " + message.getMessageId());
        response.add(StringUtils.EMPTY);
        response.add(message.getBody());
        response.add(NntpResponse.END);

        return response;
    }

    private Collection<String> responseCantFind() {
        return null;
    }

    private Collection<String> responseArticleById(NewsMessage message) {
        Collection<String> response = new LinkedList<>();

        String responseCode = NntpResponse.Article.ARTICLE_FOLLOWS_2;
        responseCode = StringUtils.replace(responseCode, "{n}", Long.toString(message.getId()));
        responseCode = StringUtils.replace(responseCode, "{message-id}", message.getMessageId());

        response.add(responseCode);
        response.add("Path: " + message.getPath());
        response.add("From: " + message.getFrom());
        response.add("Newsgroup: " + message.getGroupName());
        response.add("Subject: " + message.getSubject());
        response.add("Date: " + message.getCreatedDate());
        response.add("Message-ID: " + message.getMessageId());
        response.add(StringUtils.EMPTY);
        response.add(message.getBody());
        response.add(NntpResponse.END);

        return response;
    }
}
