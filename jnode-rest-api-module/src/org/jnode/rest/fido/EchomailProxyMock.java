package org.jnode.rest.fido;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import org.jnode.rest.di.Named;

@Named("mock-echomailProxy")
public class EchomailProxyMock implements EchomailProxy {
    @Override
    public Echomail get(Long id) {
        final Echomail echomail = new Echomail();
        final Echoarea area = new Echoarea();
        area.setName("dummy");
        echomail.setArea(area);
        return echomail;
    }
}
