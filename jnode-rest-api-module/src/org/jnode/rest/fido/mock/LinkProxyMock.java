package org.jnode.rest.fido.mock;

import jnode.dto.Link;
import jnode.logger.Logger;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.LinkProxy;

import java.util.ArrayList;
import java.util.List;

@Named("mock-linkProxy")
@Singleton
public class LinkProxyMock implements LinkProxy {

    private static final Logger LOGGER = Logger.getLogger(LinkProxyMock.class);

    private List<Link> data = new ArrayList<>();
    private long seq = 0L;

    public LinkProxyMock() {
        Link root = new Link();
        root.setId(++seq);
        root.setLinkAddress("2:5020/828.17");
        root.setPaketPassword("111111");
        data.add(root);
    }

    @Override
    public Link getByFtnAddress(String ftnAddress) {
        if (ftnAddress == null) {
            return null;
        }
        for (Link link : data) {
            if (ftnAddress.equals(link.getLinkAddress())) {
                LOGGER.l5("found " + link + " by ftn " + ftnAddress);
                return link;
            }
        }
        return null;
    }
}
