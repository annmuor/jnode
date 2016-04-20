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

        try {
            return createJsonrpc2Response(req.getID(), np);
        } catch (JSONRPC2Error jsonrpc2Error) {
            return new JSONRPC2Response(jsonrpc2Error, req.getID());
        }
    }

    private boolean roleGuard(MessageContext ctx) {
        if (ctx instanceof JnodeMessageContext) {
            JnodeMessageContext jnodeMessageContext = (JnodeMessageContext) ctx;

            if (secured() == null) {
                return true;
            }

            for (RestUser.Type type : secured()) {
                if (jnodeMessageContext.getRestUser() != null &&
                        jnodeMessageContext.getRestUser().getType() != null &&
                        jnodeMessageContext.getRestUser().getType().equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np) throws JSONRPC2Error;

    protected abstract RestUser.Type[] secured();
}
