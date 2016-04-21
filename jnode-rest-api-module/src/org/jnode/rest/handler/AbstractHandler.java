package org.jnode.rest.handler;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import org.jnode.rest.core.RPCError;
import org.jnode.rest.db.RestUser;

import java.util.Map;

public abstract class AbstractHandler implements RequestHandler {

    @Override
    public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

        if (!roleGuard(ctx)) {
            return new JSONRPC2Response(RPCError.ACCESS_DENIED, req.getID());
        }

        Map<String, Object> params = req.getNamedParams();
        NamedParamsRetriever np = new NamedParamsRetriever(params);

        RestUser restUser = getRestUserFromCtx(ctx);

            try {
            return createJsonrpc2Response(req.getID(), np, restUser);
        } catch (JSONRPC2Error jsonrpc2Error) {
            return new JSONRPC2Response(jsonrpc2Error, req.getID());
        }
    }

    private RestUser getRestUserFromCtx(MessageContext ctx) {
        RestUser restUser = null;
        if (ctx instanceof JnodeMessageContext){
            JnodeMessageContext jnodeMessageContext = (JnodeMessageContext) ctx;
            restUser = jnodeMessageContext.getRestUser();
        }
        return restUser;
    }

    private boolean roleGuard(MessageContext ctx) {

        if (secured() == null) {
            return true;
        }


        RestUser restUser = getRestUserFromCtx(ctx);

        if (restUser == null || restUser.getType() == null){
            return false;
        }

        for (RestUser.Type type : secured()) {
            if (restUser.getType().equals(type)) {
                return true;
            }
        }

        return false;
    }

    protected abstract JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np, RestUser restUser) throws JSONRPC2Error;

    protected abstract RestUser.Type[] secured();
}
