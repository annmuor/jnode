package org.jnode.rest.fido;

import jnode.dto.Link;

public interface LinkProxy {
    Link getByFtnAddress(String ftnAddress);
}
