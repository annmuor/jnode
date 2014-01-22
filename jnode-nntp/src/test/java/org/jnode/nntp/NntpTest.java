package org.jnode.nntp;

import jnode.module.JnodeModuleException;
import org.junit.Test;

public class NntpTest {

    @Test
    public void testSimple() throws JnodeModuleException {
        NntpModule module = new NntpModule("/tmp/bla.properties");
        module.start();
    }

}
