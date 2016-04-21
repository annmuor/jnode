package org.jnode.rest.fido.impl;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.EchoareaProxy;

@Named("prod-echoareaProxy")
@Singleton
public class EchoareaProxyImpl implements EchoareaProxy {
    @Override
    public Echoarea getAreaByName(String name) {
        return FtnTools.getAreaByName(name, null);
    }
}
