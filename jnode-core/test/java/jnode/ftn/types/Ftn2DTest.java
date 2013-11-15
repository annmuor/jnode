package jnode.ftn.types;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class Ftn2DTest {
    @Test
    public void testFromString() throws Exception {
        TestCase.assertEquals(new Ftn2D(5020, 828), Ftn2D.fromString("5020", "828"));
    }

    @Test
    public void testFromString2() throws Exception {
        TestCase.assertEquals(new Ftn2D(5020, 828), Ftn2D.fromString("05020", "0828"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBadString() throws Exception {
        Ftn2D.fromString("828", "");
    }
}
