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
import jnode.logger.Logger;
import jnode.store.XMLSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ConnectionStatData {
    private static final Logger logger = Logger.getLogger(ConnectionStatData.class);
    private final String statPath;

    public ConnectionStatData(String statPath) {
        this.statPath = statPath;
    }

    public List<ConnectionStatDataElement> load() {
        synchronized (ConnectionStatData.class) {
            return internalLoad();
        }
    }

    public List<ConnectionStatDataElement> loadAndDrop() {
        synchronized (ConnectionStatData.class) {
            List<ConnectionStatDataElement> result = internalLoad();
            try {
                XMLSerializer.write(new ArrayList<ConnectionStatDataElement>(), statPath);
            } catch (FileNotFoundException e) {
                logger.l2(MessageFormat.format("file {0} not found, fail clear data", statPath), e);
            }
            return result;
        }
    }

    public void store(FtnAddress ftnAddress, ConnectionStatDataElement element) {
        synchronized (ConnectionStatData.class) {
            List<ConnectionStatDataElement> elements = internalLoad();
            int pos = findPos(ftnAddress, elements);
            if (pos == -1) {
                elements.add(element);
            } else {
                elements.set(pos, element);
            }
            try {
                XMLSerializer.write(elements, statPath);
            } catch (FileNotFoundException e) {
                logger.l2(MessageFormat.format("file {0} not found, fail store data", statPath), e);
            }
        }
    }

    public int findPos(FtnAddress ftnAddress, List<ConnectionStatDataElement> elements) {
        int pos = -1;
        for (int i = 0; i < elements.size(); ++i) {
            ConnectionStatDataElement element = elements.get(i);
            if (ftnAddress == null) {
                if (element.linkStr == null) {
                    pos = i;
                    break;
                }
            } else if (element.linkStr != null) {
                if (element.linkStr.equals(ftnAddress.toString())) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }

    @SuppressWarnings("unchecked")
	private List<ConnectionStatDataElement> internalLoad() {
        List<ConnectionStatDataElement> result;
        try {
            result = new File(statPath).exists() ?
                    (List<ConnectionStatDataElement>) XMLSerializer.read(statPath) :
                    new ArrayList<ConnectionStatDataElement>();
        } catch (FileNotFoundException e) {
            logger.l2("file with stat connection ACCIDENTALLY not found", e);
            return new ArrayList<>();
        }
        return result;
    }

    public static class ConnectionStatDataElement {
        public String linkStr;
        public int bytesReceived;
        public int bytesSended;
        public int incomingOk;
        public int incomingFailed;
        public int outgoingOk;
        public int outgoingFailed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionStatData{");
        sb.append("statPath='").append(statPath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
