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

package jnode.main.threads;

import jnode.core.SysInfo;
import jnode.logger.Logger;

import java.text.MessageFormat;
import java.util.TimerTask;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class HealthReporter extends TimerTask {

    private final Logger logger = Logger
            .getLogger(getClass());

    @Override
    public void run() {

        try{
            StringBuilder sb = new StringBuilder();

            SysInfo.OpenFilesInfo openFilesInfo = SysInfo.openFilesInfo();
            if (openFilesInfo != null) {
                sb.append(MessageFormat.format("open files = {0,number,#########}/{1,number,##########}; ", openFilesInfo.getOpenFiles(), openFilesInfo.getMaxOpenFiles()));
            }

            SysInfo.MemoryInfo memoryInfo = SysInfo.memoryInfo();
            sb.append(MessageFormat.format("memory usage: max = {0,number,#########} MB, total = {1,number,#########} MB, free = {2,number,#########} MB; ",
                    memoryInfo.getMax(), memoryInfo.getTotal(), memoryInfo.getFree()));

            SysInfo.ThreadInfo threadInfo = SysInfo.threadInfo();
            sb.append(MessageFormat.format("running threads {0,number,#########}", threadInfo.getRunningThreads()));

            logger.l5(sb.toString());
        } catch(Exception consumed){
            logger.l4("got exception, continue working...", consumed);
        }

    }

}
