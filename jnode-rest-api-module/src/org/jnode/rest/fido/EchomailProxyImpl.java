package org.jnode.rest.fido;

import jnode.dto.Echomail;
import jnode.orm.ORMManager;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

@Named("prod-echomailProxy")
@Singleton
public class EchomailProxyImpl implements EchomailProxy {
    @Override
    public Echomail get(Long id) {
        return ORMManager.get(Echomail.class).getById(id);
    }
}
