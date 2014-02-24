package org.jnode.nntp.processor;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class PostProcessorTest {

    @Test
    public void testExtractNameEmail1() {

        String s = "Alex Okunevich <alexander.okunevich@gmail.com>";

        int ind1 = StringUtils.indexOf(s, "<");
        int ind2 = StringUtils.indexOf(s, ">");

        String name = StringUtils.substring(s, 0, ind1);
        String email = StringUtils.substring(s, ind1 + 1, ind2);


        System.out.println(name);
        System.out.println(email);

    }

}
