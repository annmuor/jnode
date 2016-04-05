package org.jnode.rest.handler;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import jnode.dto.Echoarea;
import org.jnode.rest.core.RPCError;
import org.jnode.rest.fido.FtnToolsProxy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

public class EchoareaPostHandler implements RequestHandler{

    @Inject
    @Named("ftnToolsProxy")
    private FtnToolsProxy ftnToolsProxy;

    @Override
    public String[] handledRequests() {
        return new String[]{"echoarea.post"};
    }

    @Override
    public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

        Map<String,Object> params = req.getNamedParams();
        NamedParamsRetriever np = new NamedParamsRetriever(params);
        Long id;
        try {

            Echoarea echoarea = ftnToolsProxy.getAreaByName(np.getString("echoarea"), null);
            if (echoarea == null){
                return new JSONRPC2Response(RPCError.ECHOARE_NOT_FOUND, req.getID());
            }

            id = ftnToolsProxy.writeEchomail(echoarea, np.getString("subject"),
                    np.getString("body"),
                    np.getOptString("fromName", ftnToolsProxy.defaultEchoFromName()),
                    np.getOptString("toName", ftnToolsProxy.defaultEchoToName())
                    );


        } catch (JSONRPC2Error jsonrpc2Error) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        }


        return new JSONRPC2Response(id, req.getID());
    }

    public void setFtnToolsProxy(FtnToolsProxy ftnToolsProxy) {
        this.ftnToolsProxy = ftnToolsProxy;
    }


}
