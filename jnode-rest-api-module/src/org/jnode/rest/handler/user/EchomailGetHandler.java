package org.jnode.rest.handler.user;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import jnode.dto.Echomail;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.fido.EchomailProxy;
import org.jnode.rest.handler.AbstractHandler;
import org.jnode.rest.mapper.EchomailMapper;

public class EchomailGetHandler extends AbstractHandler {

    @Inject
    @Named("echomailProxy")
    private EchomailProxy echomailProxy;

    @Inject
    @Named("echomailMapper")
    private EchomailMapper echomailMapper;

    @Override
    public String[] handledRequests() {
        return new String[]{"echomail.get"};
    }

    @Override
    protected JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np) throws JSONRPC2Error {
        Echomail echomail = echomailProxy.get(np.getLong("id"));
        return new JSONRPC2Response(echomailMapper.toJsonType(echomail), reqID);
    }

    public void setEchomailProxy(EchomailProxy echomailProxy) {
        this.echomailProxy = echomailProxy;
    }


    public void setEchomailMapper(EchomailMapper echomailMapper) {
        this.echomailMapper = echomailMapper;
    }
}
