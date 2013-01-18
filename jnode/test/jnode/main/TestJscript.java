package jnode.main;

import org.junit.Test;


public class TestJscript {


    @Test()
    public void normalFlow() throws Exception {

        assert true;
    }

    @Test(timeout = 10000L)
    public void testTimeout() throws Exception {


        assert true;
    }

    @Test(expected = NullPointerException.class)
    public void gotError() {
        throw new NullPointerException();
        //assert false;
    }

}