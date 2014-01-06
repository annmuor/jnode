package jnode.ftn.types;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class FtnAddressTest {
    @Test
    public void testToString() throws Exception {
        FtnAddress ftnAddress = new FtnAddress(2, 5020, 828, 17 );
        TestCase.assertEquals("2:5020/828.17", ftnAddress.toString());
    }

    @Test
    public void testToString2() throws Exception {
        FtnAddress ftnAddress = new FtnAddress(2, 5020, 828, 0 );
        TestCase.assertEquals("2:5020/828", ftnAddress.toString());
    }

    @Test
    public void testFromString() throws Exception {
        FtnAddress ftnAddress = new FtnAddress("2:5020/828.17");
        TestCase.assertEquals("2:5020/828.17", ftnAddress.toString());
    }

    @Test
    public void testFromString2() throws Exception {
        FtnAddress ftnAddress = new FtnAddress("2:5020/828");
        TestCase.assertEquals("2:5020/828", ftnAddress.toString());
    }

}
