package org.jnode.rest.core;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;

public final class RPCError {

    public static final JSONRPC2Error ECHOAREA_NOT_FOUND = new JSONRPC2Error(-10000, "Echoarea not found");

    private RPCError() {
    }


}
