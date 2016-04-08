package org.jnode.rest.core;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;

public final class RPCError {

    public static final JSONRPC2Error ECHOAREA_NOT_FOUND = new JSONRPC2Error(-10000, "Echoarea not found");
    public static final JSONRPC2Error BAD_AUTH_HEADER = new JSONRPC2Error(-10001, "Bad auth header");

    private RPCError() {
    }


}
