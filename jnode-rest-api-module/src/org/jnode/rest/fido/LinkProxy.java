package org.jnode.rest.fido;

import jnode.dto.Link;

import java.util.List;

public interface LinkProxy {

    Link getByFtnAddress(String ftnAddress);

    List<Link> getAll();

    void create(String linkName, String linkAddress, String paketPassword, String protocolPassword,
                String protocolAddress, int protocolPort) throws AlreadyExist;

    class AlreadyExist extends Exception {
        public AlreadyExist() {
            super();
        }

        public AlreadyExist(String message) {
            super(message);
        }
    }
}
