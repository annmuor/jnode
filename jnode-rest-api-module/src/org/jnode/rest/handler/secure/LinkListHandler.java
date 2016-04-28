package org.jnode.rest.handler.secure;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import jnode.dto.Link;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.fido.LinkProxy;
import org.jnode.rest.handler.AbstractHandler;

import java.util.List;

public class LinkListHandler extends AbstractHandler {

    @Inject
    @Named("linkProxy")
    private LinkProxy linkProxy;

    @Override
    protected JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np, RestUser restUser) throws JSONRPC2Error {

        List<Link> links = linkProxy.getAll();

        return new JSONRPC2Response(links, reqID);
    }

    @Override
    protected RestUser.Type[] secured() {
        return new RestUser.Type[]{RestUser.Type.ADMIN};
    }

    @Override
    public String[] handledRequests() {
        return new String[]{"link.list"};
    }

    public void setLinkProxy(LinkProxy linkProxy) {
        this.linkProxy = linkProxy;
    }

}
