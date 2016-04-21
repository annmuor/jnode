package org.jnode.rest.fido.impl;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.EchomailProxy;

@Named("prod-echomailProxy")
@Singleton
public class EchomailProxyImpl implements EchomailProxy {
    @Override
    public Echomail get(Long id) {
        return ORMManager.get(Echomail.class).getById(id);
    }

    @Override
    public Long writeEchomail(Echoarea area, String subject, String text, String fromName, String toName, String fromFTN, String tearline, String origin) {
        return FtnTools.writeEchomail(area, subject, text, fromName, toName, fromFTN, tearline, origin);
    }
}
