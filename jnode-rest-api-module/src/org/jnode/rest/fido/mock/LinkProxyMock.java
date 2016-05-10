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

        Link nopoint = new Link();
        nopoint.setId(++seq);
        nopoint.setLinkAddress("2:5020/2150");
        nopoint.setPaketPassword("222222");
        data.add(nopoint);

        Link noroot = new Link();
        noroot.setId(++seq);
        noroot.setLinkAddress("2:5020/828.18");
        noroot.setPaketPassword("111111");
        data.add(noroot);
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

    @Override
    public List<Link> getAll() {
        return data;
    }

    @Override
    public void create(String linkName, String linkAddress, String paketPassword, String protocolPassword,
                       String protocolAddress, int protocolPort) throws AlreadyExist {

        Link test = getByFtnAddress(linkAddress);
        if (test != null){
            throw new AlreadyExist(linkAddress);
        }

        Link e = new Link();
        e.setId(++seq);
        e.setLinkAddress(linkAddress);
        e.setLinkName(linkName);
        e.setPaketPassword(paketPassword);
        e.setProtocolAddress(protocolAddress);
        e.setProtocolHost(protocolAddress);
        e.setProtocolPort(protocolPort);

        data.add(e);
    }
}
