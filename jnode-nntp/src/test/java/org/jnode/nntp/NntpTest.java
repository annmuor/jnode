package org.jnode.nntp;

import jnode.logger.Logger;
import jnode.module.JnodeModuleException;
import org.junit.Test;

public class NntpTest {

    private Logger logger = Logger.getLogger(NntpTest.class);

    public static void main(String[] args) {
        try {
            NntpModule module = new NntpModule("/tmp/bla.properties");
            module.start();
        } catch (JnodeModuleException e) {
            e.printStackTrace();
        }
    }
}
