package org.jnode.rest.fido;

import jnode.dto.Echoarea;
import jnode.dto.Link;
import org.jnode.rest.di.Named;

@Named("mock-ftnToolsProxy")
public class FtnToolsProxyMock implements FtnToolsProxy{
    @Override
    public Echoarea getAreaByName(String name, Link link) {
        return new Echoarea();
    }

    @Override
    public Long writeEchomail(Echoarea area, String subject, String text, String fromName, String toName, String fromFTN, String tearline, String origin) {
        return 1L;
    }

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
