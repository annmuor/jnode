package org.jnode.rest.route;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import jnode.logger.Logger;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.IOUtils;
import org.jnode.rest.core.StringUtils;
import org.jnode.rest.di.Injector;
import org.jnode.rest.handler.EchomailGetHandler;
import org.jnode.rest.handler.EchomailPostHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

public class MainServlet extends HttpServlet {

    private static final int NUMBER_OF_AUTHENTICATION_FIELDS = 2;
    private static final Logger LOGGER = Logger.getLogger(MainServlet.class);
    private static final String BASIC_AUTHENTICATION_TYPE = "Basic";

    private final Dispatcher dispatcher = new Dispatcher();
    private BeanHolder beanHolder;

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            dispatcher.register(Injector.inject(new EchomailPostHandler()));
            dispatcher.register(Injector.inject(new EchomailGetHandler()));
            beanHolder = Injector.inject(new BeanHolder());
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ServletException(e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String encodedHeader = StringUtils.substringAfter(request.getHeader("Authorization"), "Basic");
        if(encodedHeader != null){
            encodedHeader = encodedHeader.trim();
        }
        final String decodedHeader = decodeHeader(encodedHeader);

        if (notAuthenticatedWith(credentialsFrom(decodedHeader)))
        {
            response.setHeader("WWW-Authenticate", BASIC_AUTHENTICATION_TYPE);
            response.setStatus(Http.NOT_AUTH);
            return;
        }


        response.setContentType("application/json");
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

        JSONRPC2Response resp = dispatcher.process(reqIn, null);
        if (resp.getError() != null && resp.getError().getCode() == -32601){
            response.setStatus(Http.NOT_FOUND);
        } else {
            response.setStatus(Http.OK);
        }
        final String result = resp.toString();
        LOGGER.l5("response: " + result);
        response.getWriter().print(result);
    }

    private String decodeHeader(final String encodedHeader)
    {
        if (StringUtils.isEmpty(encodedHeader)){
            return null;
        }
        return new String(Base64.getDecoder().decode(encodedHeader));
    }

    private boolean notAuthenticatedWith(final String[] credentials)
    {
        return !authenticatedWith(credentials);
    }

    private boolean authenticatedWith(final String[] credentials)
    {
        if (credentials != null && credentials.length == NUMBER_OF_AUTHENTICATION_FIELDS) {
            final String submittedUsername = credentials[0];
            final String submittedPassword = credentials[1];

            char[] pwd = beanHolder.getPwdProvider().getPwd(submittedUsername);

            LOGGER.l5("submittedUsername = " + submittedUsername);

            return pwd != null && StringUtils.equals("MD5-" + submittedPassword, new String(pwd));

        }
        else
        {
            return false;
        }
    }

    private String[] credentialsFrom(final String decoderHeader)
    {
        if (decoderHeader != null){
            return decoderHeader.split(":");
        }
        return null;
    }

}
