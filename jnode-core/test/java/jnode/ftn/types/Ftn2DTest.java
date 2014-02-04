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
