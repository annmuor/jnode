package org.jnode.rest.route;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import jnode.logger.Logger;
import org.jnode.rest.core.CryptoUtils;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.RPCError;
import org.jnode.rest.core.StringUtils;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Injector;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

public class SecureAuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(SecureAuthFilter.class);
    private static final String BASIC_AUTHENTICATION_TYPE = "Basic";
    private BeanHolder beanHolder;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            beanHolder = Injector.inject(new BeanHolder());
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        String encodedHeader = StringUtils.substringAfter(request.getHeader("Authorization"), "Basic");
        if (encodedHeader != null) {
            encodedHeader = encodedHeader.trim();
        }

        final String decodedHeader;
        try {
            decodedHeader = decodeHeader(encodedHeader);
        } catch (IllegalArgumentException e) {
            response.setStatus(Http.BAD_REQUEST);
            JSONRPC2Response respOut = new JSONRPC2Response(RPCError.BAD_AUTH_HEADER, null);
            final String result = respOut.toString();
            LOGGER.l5("bad auth response: " + result, e);
            response.getWriter().print(result);
            return;
        }

        final RestUser restUser = authenticatedWith(credentialsFrom(decodedHeader));
        if (restUser == null) {
            response.setHeader("WWW-Authenticate", BASIC_AUTHENTICATION_TYPE);
            response.setStatus(Http.NOT_AUTH);
            return;
        }

        request.setAttribute("REST_USER", restUser);

        filterChain.doFilter(request, response);
    }

    private String decodeHeader(final String encodedHeader) {
        if (StringUtils.isEmpty(encodedHeader)) {
            return null;
        }
        return new String(Base64.getDecoder().decode(encodedHeader));
    }

    private RestUser authenticatedWith(final String pwd) {
        if (pwd != null) {
            return beanHolder.getPwdProvider().isAuth(CryptoUtils.makeToken(pwd));
        }
        return null;
    }

    private String credentialsFrom(final String decoderHeader) {
        return decoderHeader;
    }


    @Override
    public void destroy() {

    }
}
