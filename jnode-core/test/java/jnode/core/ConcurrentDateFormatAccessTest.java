package jnode.core;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ConcurrentDateFormatAccessTest {


    @Test
    public void testConvertStringToDate() throws Exception {
        ConcurrentDateFormatAccess df = new ConcurrentDateFormatAccess("dd-MM-yy HH:mm:ss");
        String s = "25-01-14 17:02:03";
        TestCase.assertEquals(new Date(2014 - 1900, 0, 25, 17, 2, 3), df.parse(s));
    }

    @Test
    public void testConvertDateToString() throws Exception {
        ConcurrentDateFormatAccess df = new ConcurrentDateFormatAccess("dd-MM-yy HH:mm:ss");
        Date d = new Date(2014 - 1900, 0, 25, 17, 2, 3);
        TestCase.assertEquals("25-01-14 17:02:03", df.format(d));
    }

    @Test
    public void testLoad() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();

        final ConcurrentDateFormatAccess df = new ConcurrentDateFormatAccess("dd-MM-yy HH:mm:ss");

        for (int i = 0; i < 20; ++i) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 100; ++j) {
                        Date d = new Date(2014 - 1900, 0, 25, 17, 2, 3);
                        TestCase.assertEquals("25-01-14 17:02:03", df.format(d));

                    }
                }
            });
        }
    }

}
