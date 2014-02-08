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

package jnode.logger;

import jnode.core.FileUtils;
import jnode.main.threads.ThreadPool;

import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Redirector implements Runnable {
    private static final long MILLISEC_IN_DAY = 86400000L;

    private final Logger logger = Logger.getLogger(Redirector.class);
    private final String pathPrefix;
    private final String zipPrefix;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");

    private String lastLogFilename;

    public Redirector(String pathPrefix, String zipPrefix) {
        this.pathPrefix = pathPrefix;
        this.zipPrefix = zipPrefix;
    }

    public void invoke() {

        if (pathPrefix == null) {
            return;
        }

        redirect();

        Date showDate = getNextLaunchDate();
        Date now = new Date();
        long initialDelay = showDate.getTime() - now.getTime();
        if (initialDelay < 0) {
            initialDelay = 0;
        }

        logger.l3("next log redirect will run at " + showDate
                + " and every 1 day after");
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(this,
                initialDelay, MILLISEC_IN_DAY, TimeUnit.MILLISECONDS);
    }

    private String redirect() {

        String result = lastLogFilename;
        lastLogFilename = DATE_FORMAT.format(new Date());
        String logpath = fullLogFileName(lastLogFilename);

        try {
            PrintStream out = new PrintStream(
                    new BufferedOutputStream(
                            new FileOutputStream(logpath)), true, "UTF8");
            System.setOut(out);
            System.setErr(out);
            logger.l5("log redirected to " + logpath);

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logger.l1(MessageFormat.format("fail redirect log to {0}", logpath), e);
        }

        return result;
    }

    private String fullLogFileName(String logFileName) {
        return pathPrefix + logFileName + ".log";
    }

    private String fullZipFileName(String logFileName) {
        return zipPrefix + logFileName + ".zip";
    }

    @Override
    public void run() {
        PrintStream oldOut = System.out;
        logger.l5("oldOut " + oldOut);
        final String oldLogName = redirect();
        oldOut.close();
        logger.l5("close " + oldOut);

        if (zipPrefix != null && zipPrefix.length() != 0) {
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    String zipPath = fullZipFileName(oldLogName);
                    String logPath = fullLogFileName(oldLogName);
                    try {
                        FileUtils.zipFile(logPath,
                                zipPath, new File(logPath).getName());
                        logger.l5(MessageFormat.format("zip file {0} to {1}",
                                logPath, zipPath));
                    } catch (IOException e) {
                        logger.l1(MessageFormat.format("fail zip file {0} to {1}",
                                logPath, zipPath), e);
                    }

                }
            });
        }

    }

    private static Date getNextLaunchDate() {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return new Date(calendar.getTime().getTime() + MILLISEC_IN_DAY);
    }
}
