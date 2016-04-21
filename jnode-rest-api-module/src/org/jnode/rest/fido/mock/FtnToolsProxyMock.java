package org.jnode.rest.fido.mock;

import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.FtnToolsProxy;

@Named("mock-ftnToolsProxy")
@Singleton
public class FtnToolsProxyMock implements FtnToolsProxy {

    @Override
    public String defaultEchoFromName() {
        return "me";
    }

    @Override
    public String defaultEchoToName() {
        return "All";
    }

    @Override
    public String defaultFromFtn() {
        return "2:5020/828";
    }

    @Override
    public String defaultTearline() {
        return "blabla";
    }

    @Override
    public String defaultOrigin() {
        return "no origin";
    }
}
