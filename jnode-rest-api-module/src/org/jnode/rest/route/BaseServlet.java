package org.jnode.rest.route;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import jnode.logger.Logger;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.IOUtils;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.handler.JnodeMessageContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BaseServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONRPC2Request reqIn;

        try {

            final String body = IOUtils.readFullyAsString(request.getInputStream(), "UTF-8");
            LOGGER.l5("request body: " + body);
            reqIn = JSONRPC2Request.parse(body);

        } catch (JSONRPC2ParseException e) {
            response.setStatus(Http.BAD_REQUEST);
            JSONRPC2Response respOut = new JSONRPC2Response(JSONRPC2Error.INVALID_REQUEST, null);
            final String result = respOut.toString();
            LOGGER.l5("error response: " + result, e);
            response.getWriter().print(result);
            return;
        }

        RestUser restUser = (RestUser) request.getAttribute("REST_USER");
        request.removeAttribute("REST_USER");

        JSONRPC2Response resp = getDispatcher().process(reqIn, new JnodeMessageContext(request, restUser));
        if (resp.getError() != null && resp.getError().getCode() == -32601) {
            response.setStatus(Http.NOT_FOUND);
        } else {
            response.setStatus(Http.OK);
        }
        final String result = resp.toString();
        LOGGER.l5("response: " + result);
        response.getWriter().print(result);
    }

    protected abstract Dispatcher getDispatcher();
}
