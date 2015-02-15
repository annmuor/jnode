package org.jnode.nntp;

import jnode.module.JnodeModuleException;

public class NntpCheck {

    public static void main(String[] args) {
        try {
            NntpModule module = new NntpModule("nntp_module.conf");
            module.start();
        } catch (JnodeModuleException e) {
            e.printStackTrace();
        }
    }
}
