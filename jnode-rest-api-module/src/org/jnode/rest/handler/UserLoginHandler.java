package org.jnode.rest.handler;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

public class UserLoginHandler extends AbstractHandler{
    @Override
    protected JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np) throws JSONRPC2Error {


        return null;

    }

    @Override
    public String[] handledRequests() {
        return new String[]{"user.login"};
    }
}
