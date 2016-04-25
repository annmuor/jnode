package org.jnode.rest.core;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;

public final class RPCError {

    public static final int CODE_ECHOAREA_NOT_FOUND = -10000;
    public static final int CODE_BAD_AUTH_HEADER = -10001;
    public static final int CODE_EMPTY_LOGIN = -10002;
    public static final int CODE_INVALID_PARAMS = -32602;
    public static final int CODE_BAD_CREDENTIALS = -10003;

    public static final JSONRPC2Error ECHOAREA_NOT_FOUND = new JSONRPC2Error(CODE_ECHOAREA_NOT_FOUND, "Echoarea not found");
    public static final JSONRPC2Error BAD_AUTH_HEADER = new JSONRPC2Error(CODE_BAD_AUTH_HEADER, "Bad auth header");
    public static final JSONRPC2Error EMPTY_LOGIN = new JSONRPC2Error(CODE_EMPTY_LOGIN, "Empty login");
    public static final JSONRPC2Error BAD_CREDENTIALS = new JSONRPC2Error(CODE_BAD_CREDENTIALS,  "Bad credentials");
    public static final JSONRPC2Error ACCESS_DENIED = new JSONRPC2Error(-10004, "Access denied");

    private RPCError() {
    }


}
