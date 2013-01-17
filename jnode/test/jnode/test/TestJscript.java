package jnode.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestJscript{

    @Before
    public void init(){
    }

    @After
    public void destroy(){
    }

    @Test()
    public void normalFlow(){
        assert true;
    }

    @Test(timeout = 10000L)
    public void testTimeout(){
        assert true;
    }

    @Test(expected = NullPointerException.class)
    public void gotError(){
         throw new NullPointerException();
         //assert false;
    }

}