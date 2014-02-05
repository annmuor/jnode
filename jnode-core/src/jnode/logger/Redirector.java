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
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");

    public Redirector(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public void invoke(){
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

    private void redirect() {

        String logpath = pathPrefix + DATE_FORMAT.format(new Date()) + ".log";

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
    }

    @Override
    public void run() {
        PrintStream oldOut = System.out;
        logger.l5("oldOut " + oldOut);
        redirect();
        oldOut.close();
        logger.l5("close " + oldOut);
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
