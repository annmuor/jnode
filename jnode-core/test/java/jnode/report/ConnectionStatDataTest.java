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

package jnode.report;

import jnode.ftn.types.FtnAddress;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ConnectionStatDataTest {

    private String path;
    private ConnectionStatData.ConnectionStatDataElement e1;
    private ConnectionStatData.ConnectionStatDataElement e2;

    @Before
    public void setUp() throws Exception {
        File file = File.createTempFile("test", ".xml");
        path = file.getAbsolutePath();
        file.delete();

        e1 = new ConnectionStatData.ConnectionStatDataElement();
        e1.bytesReceived = 1;
        e1.bytesSended = 2;
        e1.incomingFailed = 3;
        e1.incomingOk = 4;
        e1.linkStr = "2:5020/828.17";
        e1.outgoingFailed = 5;
        e1.outgoingOk = 6;

        e2 = new ConnectionStatData.ConnectionStatDataElement();
        e2.bytesReceived = 11;
        e2.bytesSended = 21;
        e2.incomingFailed = 31;
        e2.incomingOk = 41;
        e2.linkStr = "2:5020/828.18";
        e2.outgoingFailed = 51;
        e2.outgoingOk = 61;
    }

    @After
    public void tearDown() throws Exception {
        new File(path).delete();
    }

    @Test
    public void testLoad() throws Exception {
        URL url = ConnectionStatDataTest.class.getResource("testload.xml");
        Path src = new File(url.getPath()).toPath();
        Path dest = new File(path).toPath();
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

        ConnectionStatData data = new ConnectionStatData(path);
        List<ConnectionStatData.ConnectionStatDataElement> loaded = data.load();

        check(loaded);
    }

    @Test
    public void testLoadAndDrop() throws Exception {
        URL url = ConnectionStatDataTest.class.getResource("testload.xml");
        Path src = new File(url.getPath()).toPath();
        Path dest = new File(path).toPath();
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

        ConnectionStatData data = new ConnectionStatData(path);
        List<ConnectionStatData.ConnectionStatDataElement> loaded = data.loadAndDrop();

        check(loaded);

        loaded = data.load();
        TestCase.assertNotNull(loaded);
        TestCase.assertEquals(0, loaded.size());

    }

    @Test
    public void testStore() throws Exception {
        ConnectionStatData data = new ConnectionStatData(path);

        data.store(new FtnAddress("2:5020/828.17"), e1);
        data.store(new FtnAddress("2:5020/828.18"), e2);

        List<ConnectionStatData.ConnectionStatDataElement> loaded = data.load();

        check(loaded);

    }

    @Test
    public void testStore2() throws Exception {
        ConnectionStatData data = new ConnectionStatData(path);

        data.store(new FtnAddress("2:5020/828.17"), e1);
        e2.linkStr = null;
        data.store(null, e2);

        List<ConnectionStatData.ConnectionStatDataElement> loaded = data.load();

        TestCase.assertNotNull(loaded);
        TestCase.assertEquals(2, loaded.size());

        TestCase.assertEquals(1, loaded.get(0).bytesReceived);
        TestCase.assertEquals(2, loaded.get(0).bytesSended);
        TestCase.assertEquals(3, loaded.get(0).incomingFailed);
        TestCase.assertEquals(4, loaded.get(0).incomingOk);
        TestCase.assertEquals("2:5020/828.17", loaded.get(0).linkStr);
        TestCase.assertEquals(5, loaded.get(0).outgoingFailed);
        TestCase.assertEquals(6, loaded.get(0).outgoingOk);

        TestCase.assertEquals(11, loaded.get(1).bytesReceived);
        TestCase.assertEquals(21, loaded.get(1).bytesSended);
        TestCase.assertEquals(31, loaded.get(1).incomingFailed);
        TestCase.assertEquals(41, loaded.get(1).incomingOk);
        TestCase.assertNull(loaded.get(1).linkStr);
        TestCase.assertEquals(51, loaded.get(1).outgoingFailed);
        TestCase.assertEquals(61, loaded.get(1).outgoingOk);
    }

    private void check(List<ConnectionStatData.ConnectionStatDataElement> loaded) {
        TestCase.assertNotNull(loaded);
        TestCase.assertEquals(2, loaded.size());

        TestCase.assertEquals(1, loaded.get(0).bytesReceived);
        TestCase.assertEquals(2, loaded.get(0).bytesSended);
        TestCase.assertEquals(3, loaded.get(0).incomingFailed);
        TestCase.assertEquals(4, loaded.get(0).incomingOk);
        TestCase.assertEquals("2:5020/828.17", loaded.get(0).linkStr);
        TestCase.assertEquals(5, loaded.get(0).outgoingFailed);
        TestCase.assertEquals(6, loaded.get(0).outgoingOk);

        TestCase.assertEquals(11, loaded.get(1).bytesReceived);
        TestCase.assertEquals(21, loaded.get(1).bytesSended);
        TestCase.assertEquals(31, loaded.get(1).incomingFailed);
        TestCase.assertEquals(41, loaded.get(1).incomingOk);
        TestCase.assertEquals("2:5020/828.18", loaded.get(1).linkStr);
        TestCase.assertEquals(51, loaded.get(1).outgoingFailed);
        TestCase.assertEquals(61, loaded.get(1).outgoingOk);
    }

    @Test
    public void testFindPos() throws Exception {
        ConnectionStatData data = new ConnectionStatData(path);

        List<ConnectionStatData.ConnectionStatDataElement> d = new ArrayList<>();
        d.add(e1);
        d.add(e2);

        int pos = data.findPos(new FtnAddress("2:5020/828.17"), d);
        TestCase.assertEquals(0, pos);
        pos = data.findPos(new FtnAddress("2:5020/828.18"), d);
        TestCase.assertEquals(1, pos);
        pos = data.findPos(new FtnAddress("2:5020/828.19"), d);
        TestCase.assertEquals(-1, pos);

    }
}
