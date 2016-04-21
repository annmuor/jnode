package org.jnode.rest.fido.impl;

import jnode.dto.Link;
import jnode.orm.ORMManager;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.LinkProxy;

@Named("prod-linkProxy")
@Singleton
public class LinkProxyImpl implements LinkProxy {
    @Override
    public Link getByFtnAddress(String ftnAddress) {
        return ORMManager.get(Link.class).getFirstAnd("ftn_address", "=", ftnAddress);
    }
}
