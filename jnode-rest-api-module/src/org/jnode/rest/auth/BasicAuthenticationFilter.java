package org.jnode.rest.auth;

import jnode.logger.Logger;
import org.jnode.rest.core.StringUtils;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Base64;

@Named("basicAuthenticationFilter")
public class BasicAuthenticationFilter extends Filter {

    private static final Logger LOGGER = Logger.getLogger(BasicAuthenticationFilter.class);

    private static final String BASIC_AUTHENTICATION_TYPE = "Basic";

    private static final int NUMBER_OF_AUTHENTICATION_FIELDS = 2;

    private static final String ACCEPT_ALL_TYPES = "*";

    @Inject @Named("pwdProvider")
    private PwdProvider pwdProvider;

    public BasicAuthenticationFilter()
    {
        this(SparkUtils.ALL_PATHS);
    }

    private BasicAuthenticationFilter(final String path)
    {
        super(path, ACCEPT_ALL_TYPES);
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

    public void setPwdProvider(PwdProvider pwdProvider) {
        this.pwdProvider = pwdProvider;
    }

    @Override
    public String toString() {
        return "BasicAuthenticationFilter{" +
                "pwdProvider=" + pwdProvider +
                "} " + super.toString();
    }
}
