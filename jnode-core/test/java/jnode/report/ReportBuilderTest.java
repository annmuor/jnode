package jnode.report;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class ReportBuilderTest {
    @Test
    public void testAddItem() throws Exception {
        StringBuilder sb = new StringBuilder();
        ReportBuilder.addItem(sb, "123", 5);
        TestCase.assertEquals("123  ", sb.toString());
    }

    @Test
    public void testAddItemShort() throws Exception {
        StringBuilder sb = new StringBuilder();
        ReportBuilder.addItem(sb, "12345", 3);
        TestCase.assertEquals("123", sb.toString());
    }

    @Test
    public void testAddCenterItem() throws Exception {
        StringBuilder sb = new StringBuilder();
        ReportBuilder.addCenterItem(sb, "123", 5);
        TestCase.assertEquals(" 123 ", sb.toString());
    }

    @Test
    public void testAddCenterItem2() throws Exception {
        StringBuilder sb = new StringBuilder();
        ReportBuilder.addCenterItem(sb, "123", 6);
        TestCase.assertEquals(" 123  ", sb.toString());
    }

    @Test
    public void testVer() throws Exception {
        StringBuilder sb = new StringBuilder();
        ReportBuilder.addVer(sb);
        TestCase.assertEquals("|", sb.toString());
    }

    @Test
    public void testNewLine() throws Exception {
        StringBuilder sb = new StringBuilder();
        ReportBuilder.newLine(sb);
        TestCase.assertEquals("\n", sb.toString());
    }

    @Test
    public void testWidth() throws Exception {
        ReportBuilder builder = new ReportBuilder();
        builder.setColLength(Arrays.asList(1, 2, 4));
        TestCase.assertEquals(11, builder.getWidth());
    }

    @Test
    public void testHorLine() throws Exception {
        ReportBuilder builder = new ReportBuilder();
        builder.setColLength(Arrays.asList(1, 2, 4));
        StringBuilder sb = new StringBuilder();
        builder.horLine(sb);
        TestCase.assertEquals("+-+--+----+\n", sb.toString());
    }
}
