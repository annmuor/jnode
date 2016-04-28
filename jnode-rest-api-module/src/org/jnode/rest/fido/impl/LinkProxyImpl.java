package org.jnode.rest.fido.impl;

import jnode.dto.Link;
import jnode.orm.ORMManager;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.LinkProxy;

import java.util.List;

@Named("prod-linkProxy")
@Singleton
public class LinkProxyImpl implements LinkProxy {
    @Override
    public Link getByFtnAddress(String ftnAddress) {
        return ORMManager.get(Link.class).getFirstAnd("ftn_address", "=", ftnAddress);
    }

    @Override
    public List<Link> getAll() {
        return ORMManager.get(Link.class).getAll();
    }
}
