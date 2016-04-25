package org.jnode.rest.fido;

import jnode.ftn.types.FtnAddress;

public interface FtnToolsProxy {
    String defaultEchoFromName();
    String defaultEchoToName();
    String defaultTearline();
    String defaultOrigin();
    boolean isOurPoint(FtnAddress routeTo);
}
