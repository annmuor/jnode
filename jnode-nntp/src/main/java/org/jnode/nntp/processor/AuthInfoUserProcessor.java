package org.jnode.nntp.processor;

import com.google.common.collect.Lists;
import jnode.event.Notifier;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.Processor;
import org.jnode.nntp.event.AuthUserEvent;
import org.jnode.nntp.exception.UnknownCommandException;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NntpResponse;

import java.util.Collection;

public class AuthInfoUserProcessor implements Processor {


    @Override
    public Collection<String> process(Collection<String> params, Long selectedGroupId, Long selectedArticleId, Auth auth) {
        if (params == null || params.size() != 1) {
            throw new UnknownCommandException();
        }

        String user = params.iterator().next();

        String username = StringUtils.substring(user, 0, StringUtils.indexOf(user, "@"));
        String ftnAddress = convert(user);

        Notifier.INSTANSE.notify(new AuthUserEvent(new Auth(user, username, ftnAddress)));

        Collection<String> response = Lists.newLinkedList();
        response.add(NntpResponse.AuthInfo.PASSWORD_REQUIRED);
        return response;
    }

    /**              alex.okunevich@p1.f321.n450.z2.fidonet.org
     * User example: jim.ayson@p300.f2.n750.z6.fidonet.org
     *
     * @param user
     * @return 6:450/2.300
     */
    private String convert(String user) {

        StringBuilder ftnAddress = new StringBuilder();

        String[] ftnParts = StringUtils.split(StringUtils.substring(user, StringUtils.indexOf(user, "@") + 1), ".");
        // -2 because skip fidonet.org
        // -3 because start with 0
        for (int index = ftnParts.length - 3; index >= 0; index--) {
            String part = ftnParts[index];
            if (StringUtils.contains(part, "z")) {
                // ignore
            }
            if (StringUtils.contains(part, "n")) {
                ftnAddress.append(":");
            }
            if (StringUtils.contains(part, "f")) {
                ftnAddress.append("/");
            }
            if (StringUtils.contains(part, "p")) {
                ftnAddress.append(".");
            }

            ftnAddress.append(StringUtils.substring(ftnParts[index], 1));
        }

        return ftnAddress.toString();
    }

}
