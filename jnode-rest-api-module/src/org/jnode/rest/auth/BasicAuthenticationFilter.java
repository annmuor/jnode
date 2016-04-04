package org.jnode.rest.auth;

import jnode.logger.Logger;
import org.jnode.rest.core.StringUtils;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

import java.util.Base64;

public class BasicAuthenticationFilter extends Filter {

    private static final Logger LOGGER = Logger.getLogger(BasicAuthenticationFilter.class);

    private static final String BASIC_AUTHENTICATION_TYPE = "Basic";

    private static final int NUMBER_OF_AUTHENTICATION_FIELDS = 2;

    private static final String ACCEPT_ALL_TYPES = "*";

    private final PwdProvider pwdProvider;

    public BasicAuthenticationFilter(final PwdProvider pwdProvider)
    {
        this(SparkUtils.ALL_PATHS, pwdProvider);
    }

    public BasicAuthenticationFilter(final String path, final PwdProvider pwdProvider)
    {
        super(path, ACCEPT_ALL_TYPES);
        this.pwdProvider = pwdProvider;
    }

    @Override
    public void handle(final Request request, final Response response)
    {
        String encodedHeader = StringUtils.substringAfter(request.headers("Authorization"), "Basic");
        if(encodedHeader != null){
            encodedHeader = encodedHeader.trim();
        }
        final String decodedHeader = decodeHeader(encodedHeader);

        if (notAuthenticatedWith(credentialsFrom(decodedHeader)))
        {
            response.header("WWW-Authenticate", BASIC_AUTHENTICATION_TYPE);
            halt(401);
        }
    }

    private String[] credentialsFrom(final String decoderHeader)
    {
        if (decoderHeader != null){
            return decoderHeader.split(":");
        }
        return null;
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

            char[] pwd = pwdProvider.getPwd(submittedUsername);

            LOGGER.l5("submittedUsername = " + submittedUsername);

            return pwd != null && StringUtils.equals("MD5-" + submittedPassword, new String(pwd));

        }
        else
        {
            return false;
        }
    }
}
