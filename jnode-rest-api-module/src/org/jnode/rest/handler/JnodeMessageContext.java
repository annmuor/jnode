package org.jnode.rest.handler;

import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import org.jnode.rest.db.RestUser;

import javax.servlet.http.HttpServletRequest;

public class JnodeMessageContext extends MessageContext {

    private final RestUser restUser;

    public JnodeMessageContext(HttpServletRequest httpRequest, RestUser restUser) {
        super(httpRequest);
        this.restUser = restUser;
    }

    public RestUser getRestUser() {
        return restUser;
    }

    @Override
    public String toString() {
        return "JnodeMessageContext{" +
                "restUser=" + restUser +
                "} " + super.toString();
    }
}
