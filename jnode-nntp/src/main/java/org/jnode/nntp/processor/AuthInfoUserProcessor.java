package org.jnode.nntp.processor;

import com.google.common.collect.Lists;
import jnode.event.Notifier;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.Processor;
import org.jnode.nntp.event.AuthUserEvent;
import org.jnode.nntp.exception.UnknownCommandException;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NntpResponse;
import org.jnode.nntp.util.Converter;

import java.util.Collection;

public class AuthInfoUserProcessor implements Processor {


    @Override
    public Collection<String> process(Collection<String> params, Long selectedGroupId, Long selectedArticleId, Auth auth) {
        if (params == null || params.size() != 1) {
            throw new UnknownCommandException();
        }

        String user = params.iterator().next();

        String username = StringUtils.substring(user, 0, StringUtils.indexOf(user, "@"));
        String ftnAddress = Converter.convertEmailToFtn(user);

        Notifier.INSTANSE.notify(new AuthUserEvent(new Auth(user, username, ftnAddress)));

        Collection<String> response = Lists.newLinkedList();
        response.add(NntpResponse.AuthInfo.PASSWORD_REQUIRED);
        return response;
    }
}
