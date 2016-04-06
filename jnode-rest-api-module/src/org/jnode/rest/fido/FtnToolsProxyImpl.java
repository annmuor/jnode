package org.jnode.rest.fido;

import jnode.dto.Echoarea;
import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.main.MainHandler;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;


@Named("prod-ftnToolsProxy")
@Singleton
public class FtnToolsProxyImpl implements FtnToolsProxy{
    @Override
    public Echoarea getAreaByName(String name, Link link) {
        return FtnTools.getAreaByName(name, link);
    }

    @Override
    public Long writeEchomail(Echoarea area, String subject, String text, String fromName, String toName,
                              String fromFTN, String tearline, String origin) {
        return FtnTools.writeEchomail(area, subject, text, fromName, toName, fromFTN, tearline, origin);
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
