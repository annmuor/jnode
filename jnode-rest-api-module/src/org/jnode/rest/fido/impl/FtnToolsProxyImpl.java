package org.jnode.rest.fido.impl;

import jnode.ftn.FtnTools;
import jnode.main.MainHandler;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.FtnToolsProxy;


@Named("prod-ftnToolsProxy")
@Singleton
public class FtnToolsProxyImpl implements FtnToolsProxy {

    @Override
    public String defaultEchoFromName() {
        return MainHandler.getCurrentInstance()
                .getInfo().getStationName();
    }

    @Override
    public String defaultEchoToName() {
        return "All";
    }

    @Override
    public String defaultFromFtn() {
        return FtnTools.getPrimaryFtnAddress().toString();
    }

    @Override
    public String defaultTearline() {
        return MainHandler.getCurrentInstance().getInfo().getStationName();
    }

    @Override
    public String defaultOrigin() {
        return MainHandler.getVersion() + " ("+ FtnTools.getPrimaryFtnAddress().toString() + ")";
    }
}
