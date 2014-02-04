/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.core;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
@SuppressWarnings("deprecation")
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
