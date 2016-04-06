package org.jnode.rest.handler;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import jnode.dto.Echomail;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.fido.EchomailProxy;

import java.util.Map;

public class EchomailGetHandler implements RequestHandler {

    @Inject
    @Named("echomailProxy")
    private EchomailProxy echomailProxy;

    @Override
    public String[] handledRequests() {
        return new String[]{"echomail.get"};
    }

    @Override
    public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

        Map<String, Object> params = req.getNamedParams();
        NamedParamsRetriever np = new NamedParamsRetriever(params);

        try {
            Echomail echomail = echomailProxy.get(np.getLong("id"));
            return new JSONRPC2Response(echomail, req.getID());
        } catch (JSONRPC2Error jsonrpc2Error) {
            return new JSONRPC2Response(jsonrpc2Error, req.getID());
        }
    }

    public void setEchomailProxy(EchomailProxy echomailProxy) {
        this.echomailProxy = echomailProxy;
    }

}
