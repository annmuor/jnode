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

package jnode.stat;

import jnode.event.ConnectionEndEvent;
import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.report.ConnectionStatData;
import jnode.report.ReportBuilder;
import jnode.stat.threads.StatPoster;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConnectionStat implements IStatPoster, IEventHandler {

    private static final Logger logger = Logger.getLogger(ConnectionStatData.class);
    private final String statPath = FtnTools.getInbound() + File.separator
            + "connstat.xml";


    public ConnectionStat() {
        Notifier.INSTANSE.register(ConnectionEndEvent.class, this);
    }

    public void handle(IEvent event) {
        synchronized (ConnectionStat.class) {

            if (event instanceof ConnectionEndEvent) {
                ConnectionEndEvent evt = (ConnectionEndEvent) event;

                ConnectionStatData data = new ConnectionStatData(statPath);

                ConnectionStatData.ConnectionStatDataElement current;

                List<ConnectionStatData.ConnectionStatDataElement> elements = data.load();

                int pos = data.findPos(evt.getAddress(), elements);
                if (pos == -1) {
                    current = new ConnectionStatData.ConnectionStatDataElement();
                    current.linkStr = evt.getAddress() != null ? evt.getAddress().toString() : null;
                } else {
                    current = elements.get(pos);
                }

                if (evt.isIncoming()) {
                    if (evt.isSuccess()) {
                        current.incomingOk++;
                    } else {
                        current.incomingFailed++;
                    }
                } else {
                    if (evt.isSuccess()) {
                        current.outgoingOk++;
                    } else {
                        current.outgoingFailed++;
                    }
                }
                current.bytesReceived += evt.getBytesReceived();
                current.bytesSended += evt.getBytesSended();

                data.store(evt.getAddress(), current);
            }
        }
    }

    @Override
    public String getSubject() {
        return "Daily connection stat";
    }

    public static String getText(String path, boolean reset){
        logger.l5("getText path = [" + path + "], reset = [" + reset + "]");
        ConnectionStatData data = new ConnectionStatData(path);
        logger.l5("get ConnectionStatData " + data);
        List<ConnectionStatData.ConnectionStatDataElement> elements = reset ? data.loadAndDrop() : data.load();
        logger.l5(MessageFormat.format("has {0} elements", elements != null ? elements.size() : 0));

        ReportBuilder builder = new ReportBuilder();
        builder.setColumns(Arrays.asList("Link", "I_OK", "I_FA", "O_OK", "O_FA", "BR", "BS"));
        builder.setColLength(Arrays.asList(19,    5,      5,     5,     5,    9,   9));

        Collections.sort(elements, new Comparator<ConnectionStatData.ConnectionStatDataElement>() {

            @Override
            public int compare(ConnectionStatData.ConnectionStatDataElement arg0,
                               ConnectionStatData.ConnectionStatDataElement arg1) {
                FtnAddress a1 = arg0.linkStr != null ? new FtnAddress(arg0.linkStr) : null;
                FtnAddress a2 = arg1.linkStr != null ? new FtnAddress(arg1.linkStr) : null;
                if (a1 == null && a2 != null) {
                    return 1;
                } else if (a2 == null && a1 != null) {
                    return -1;
                } else if (a1 == null && a2 == null) {
                    return 0;
                } else {
                    return new FtnTools.Ftn4DComparator().compare(a1, a2);
                }
            }

        });


        int iOkT = 0;
        int iFaT = 0;
        int oOkT = 0;
        int oFaT = 0;
        int bsT = 0;
        int brT = 0;
        for (ConnectionStatData.ConnectionStatDataElement element : elements) {
            FtnAddress link = element.linkStr != null ? new FtnAddress(element.linkStr) : null;
            String linkName = (link != null) ? link.toString()
                    : "Unknown";
            iOkT += element.incomingOk;
            iFaT += element.incomingFailed;
            oOkT += element.outgoingOk;
            oFaT += element.outgoingFailed;
            bsT += element.bytesSended;
            brT += element.bytesReceived;

            builder.printLine(
                    linkName,
                    String.valueOf(element.incomingOk),
                    String.valueOf(element.incomingFailed),
                    String.valueOf(element.outgoingOk),
                    String.valueOf(element.outgoingFailed),
                    b2s(element.bytesReceived),
                    b2s(element.bytesSended)
                    );

        }

        if (elements.size() > 0){
            builder.printHorLine();
        }

        builder.printLine(
                "Summary",
                String.valueOf(iOkT),
                String.valueOf(iFaT),
                String.valueOf(oOkT),
                String.valueOf(oFaT),
                b2s(brT),
                b2s(bsT)
        );

        return builder.getText().toString();
    }

    @Override
    public String getText() {
        return getText(statPath, true);
    }

    private static String b2s(int bytes) {
        String format = "%4.2f %s";
        String type = (bytes > 1024) ? (bytes > 1048576) ? (bytes > 1073741824) ? "Gb"
                : "Mb"
                : "Kb"
                : "B";
        float byts = bytes
                / ((bytes > 1024) ? (bytes > 1048576) ? (bytes > 1073741824) ? 1073741824.0f
                : 1048576.0f
                : 1024.0f
                : 1.0f);
        return String.format(format, byts, type).replace(',', '.');
    }

    @Override
    public void init(StatPoster poster) {

    }
}
