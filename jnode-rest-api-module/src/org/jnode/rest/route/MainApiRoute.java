package org.jnode.rest.route;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import org.jnode.rest.core.Http;
import spark.Request;
import spark.Response;
import spark.Route;

public class MainApiRoute extends Route {

    private final Dispatcher dispatcher;

    public MainApiRoute(String path, Dispatcher dispatcher) {
        super(path);
        this.dispatcher = dispatcher;
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        JSONRPC2Request reqIn = null;

        try {
            reqIn = JSONRPC2Request.parse(request.body());

        } catch (JSONRPC2ParseException e) {
            response.status(Http.BAD_REQUEST);
            JSONRPC2Response respOut = new JSONRPC2Response(JSONRPC2Error.INVALID_REQUEST, 0);
            return respOut.toString();
        }

        JSONRPC2Response resp = dispatcher.process(reqIn, null);
        response.status(Http.OK);
        return resp.toString();


    }
}
