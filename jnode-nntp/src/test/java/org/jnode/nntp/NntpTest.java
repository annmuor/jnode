package org.jnode.nntp;

import jnode.logger.Logger;
import jnode.module.JnodeModuleException;
import org.junit.Test;

public class NntpTest {

    private Logger logger = Logger.getLogger(NntpTest.class);

    @Test
    public void runServer() throws JnodeModuleException {
        NntpModule module = new NntpModule("/tmp/bla.properties");
        module.start();
    }

}
