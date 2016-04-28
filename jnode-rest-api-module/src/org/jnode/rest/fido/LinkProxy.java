package org.jnode.rest.fido;

import jnode.dto.Link;

import java.util.List;

public interface LinkProxy {
    Link getByFtnAddress(String ftnAddress);
    List<Link> getAll();
}
