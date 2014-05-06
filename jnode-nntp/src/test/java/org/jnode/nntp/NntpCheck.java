package org.jnode.nntp;

import jnode.logger.Logger;
import jnode.module.JnodeModuleException;

public class NntpCheck {

    public static void main(String[] args) {
        try {
            NntpModule module = new NntpModule("/tmp/bla.properties");
            module.start();
        } catch (JnodeModuleException e) {
            e.printStackTrace();
        }
    }
}
