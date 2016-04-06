package org.jnode.rest.route;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import jnode.logger.Logger;
import org.jnode.rest.core.Http;
import spark.Request;
import spark.Response;
import spark.Route;

public class MainApiRoute extends Route {

    private static final Logger LOGGER = Logger.getLogger(MainApiRoute.class);
    private final Dispatcher dispatcher;

    public MainApiRoute(String path, Dispatcher dispatcher) {
        super(path);
        this.dispatcher = dispatcher;
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        JSONRPC2Request reqIn;

        try {

            final String body = request.body();
            LOGGER.l5("request body: " + body);
            reqIn = JSONRPC2Request.parse(body);

        } catch (JSONRPC2ParseException e) {
            response.status(Http.BAD_REQUEST);
            JSONRPC2Response respOut = new JSONRPC2Response(JSONRPC2Error.INVALID_REQUEST, null);
            final String result = respOut.toString();
            LOGGER.l5("error response: " + result, e);
            return result;
        }

        JSONRPC2Response resp = dispatcher.process(reqIn, null);
        response.status(Http.OK);
        final String result = resp.toString();
        LOGGER.l5("response: " + result);
        return result;


    }
}
