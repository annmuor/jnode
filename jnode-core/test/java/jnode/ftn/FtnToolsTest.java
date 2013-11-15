package jnode.ftn;

import jnode.ftn.types.Ftn2D;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

/**
 * @author Kirill Temnenkov (kirill@temnenkov.com)
 */
public class FtnToolsTest {
    @Test
    public void testRead2D() throws Exception {
        List<Ftn2D> r = FtnTools.read2D("250/25 463/68 5000/111");

        TestCase.assertEquals(3, r.size());
        TestCase.assertEquals(new Ftn2D(250,25), r.get(0));
        TestCase.assertEquals(new Ftn2D(463,68), r.get(1));
        TestCase.assertEquals(new Ftn2D(5000,111), r.get(2));
    }

    @Test
    public void testRead2DSmart() throws Exception {
        List<Ftn2D> r = FtnTools.read2D("5030/2104 2404 5051/41");

        TestCase.assertEquals(3, r.size());
        TestCase.assertEquals(new Ftn2D(5030,2104), r.get(0));
        TestCase.assertEquals(new Ftn2D(5030,2404), r.get(1));
        TestCase.assertEquals(new Ftn2D(5051,41), r.get(2));
    }

    @Test
    public void testRead2DSmart2() throws Exception {
        List<Ftn2D> r = FtnTools.read2D("5020/2141 2140");

        TestCase.assertEquals(2, r.size());
        TestCase.assertEquals(new Ftn2D(5020,2141), r.get(0));
        TestCase.assertEquals(new Ftn2D(5020,2140), r.get(1));
    }

    @Test
    public void testRead2DBad() throws Exception {
        List<Ftn2D> r = FtnTools.read2D("5020/2141 sd2140 5030/141");

        TestCase.assertEquals(2, r.size());
        TestCase.assertEquals(new Ftn2D(5020,2141), r.get(0));
        TestCase.assertEquals(new Ftn2D(5030,141), r.get(1));
    }

    @Test
    public void testRead2DBad2() throws Exception {
        List<Ftn2D> r = FtnTools.read2D("5020/2141 as2140 141");

        TestCase.assertEquals(1, r.size());
        TestCase.assertEquals(new Ftn2D(5020,2141), r.get(0));
    }
}
