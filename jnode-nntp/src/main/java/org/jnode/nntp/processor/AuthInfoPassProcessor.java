package org.jnode.nntp.processor;

import com.google.common.collect.Lists;
import jnode.dto.Link;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.exception.UnknownCommandException;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;

public class AuthInfoPassProcessor implements Processor {

    private DataProvider dataProvider = new DataProviderImpl();

    @Override
    public Collection<String> process(Collection<String> params, Long selectedGroupId, Long selectedArticleId, Auth auth) {

        if (params == null || params.size() != 1) {
            throw new UnknownCommandException();
        }

        String pass = params.iterator().next();

        Link link = dataProvider.link(auth, pass);

        Collection<String> response = Lists.newLinkedList();

        if (link == null) {
            response.add(NntpResponse.AuthInfo.AUTHENTIFICATION_FAILED_OR_REJECTED);
            auth.reset();
        } else {
            auth.setLinkId(link.getId());
            response.add(NntpResponse.AuthInfo.AUTHENTIFICATION_ACCEPTED);
        }

        return response;
    }

}
