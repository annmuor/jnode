package org.jnode.nntp.response;

import org.jnode.nntp.Response;

public class InitialGreeting implements Response {

    @Override
    public String response() {
        return "200 NNTP Service Ready, posting permitted";
    }
}
