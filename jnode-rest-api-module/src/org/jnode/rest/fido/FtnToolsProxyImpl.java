package org.jnode.rest.fido;

import jnode.dto.Echoarea;
import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.main.MainHandler;

import javax.inject.Named;

@Named("prod-ftnToolsProxy")
public class FtnToolsProxyImpl implements FtnToolsProxy{
    @Override
    public Echoarea getAreaByName(String name, Link link) {
        return FtnTools.getAreaByName(name, link);
    }

    @Override
    public Long writeEchomail(Echoarea area, String subject, String text, String fromName, String toName) {
        return FtnTools.writeEchomail(area, subject, text, fromName, toName);
    }

    @Override
    public String defaultEchoFromName() {
        return MainHandler.getCurrentInstance()
                .getInfo().getStationName();
    }

    @Override
    public String defaultEchoToName() {
        return "All";
    }
}
