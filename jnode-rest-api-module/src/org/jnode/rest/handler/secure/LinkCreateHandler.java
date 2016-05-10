package org.jnode.rest.handler.secure;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import org.jnode.rest.core.RPCError;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.fido.LinkProxy;
import org.jnode.rest.handler.AbstractHandler;

public class LinkCreateHandler extends AbstractHandler {

    @Inject
    @Named("linkProxy")
    private LinkProxy linkProxy;

    @Override
    protected JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np, RestUser restUser) throws JSONRPC2Error {

        try {
            linkProxy.create(
                    np.getString("linkName"),
                    np.getString("linkAddress"),
                    np.getString("paketPassword"),
                    np.getString("protocolPassword"),
                    np.getString("protocolAddress"),
                    np.getInt("protocolPort")
                    );
        } catch (LinkProxy.AlreadyExist alreadyExist) {
            return new JSONRPC2Response(RPCError.LINK_ALREADY_EXIST, reqID);
        }

        return new JSONRPC2Response(null, reqID);
    }

    @Override
    protected RestUser.Type[] secured() {
        return new RestUser.Type[]{RestUser.Type.ADMIN};
    }

    @Override
    public String[] handledRequests() {
        return new String[]{"link.create"};
    }

    public void setLinkProxy(LinkProxy linkProxy) {
        this.linkProxy = linkProxy;
    }

}
