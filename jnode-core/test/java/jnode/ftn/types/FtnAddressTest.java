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
