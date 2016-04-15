package org.jnode.rest.core;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;

public final class RPCError {

    public static final JSONRPC2Error ECHOAREA_NOT_FOUND = new JSONRPC2Error(-10000, "Echoarea not found");
    public static final JSONRPC2Error BAD_AUTH_HEADER = new JSONRPC2Error(-10001, "Bad auth header");
    public static final JSONRPC2Error EMPTY_LOGIN = new JSONRPC2Error(-10002, "Empty login");
    public static final JSONRPC2Error BAD_CREDENTIALS = new JSONRPC2Error(-10003, "Bad credentials");
    public static final JSONRPC2Error ACCESS_DENIED = new JSONRPC2Error(-10004, "Access denied");

    private RPCError() {
    }


}
