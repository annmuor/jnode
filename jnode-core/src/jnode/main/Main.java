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

package jnode.main;

import com.j256.ormlite.logger.LocalLog;
import jnode.dto.*;
import jnode.event.Notifier;
import jnode.event.SharedModuleEvent;
import jnode.ftn.FtnTools;
import jnode.install.GUIConfigurator;
import jnode.install.InstallUtil;
import jnode.jscript.JscriptExecutor;
import jnode.logger.Logger;
import jnode.logger.Redirector;
import jnode.main.threads.*;
import jnode.module.JnodeModule;
import jnode.orm.ORMManager;
import jnode.protocol.binkp.BinkpAsyncClientPool;
import jnode.protocol.binkp.BinkpAsyncServer;
import jnode.stat.threads.StatPoster;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author kreon
 *
 */
public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	private static final String POLL_DELAY = "poll.delay";
	private static final String POLL_PERIOD = "poll.period";
	private static final String BINKD_THREADS = "binkp.threads";
	private static final String LOG_LEVEL = "log.level";
	private static final String MODULES = "modules";
	private static final String LOGFILE = "log.file";
	private static final String LOGZIPPATH = "log.zippath";

	public static void main(String[] args) {
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
		if (args.length == 0) {
			System.out.println("Usage: $0 <config-file>");
			System.exit(-1);
		}

		try {
			new MainHandler(args[0]);
			tryRedirectLog();

		} catch (IOException e) {
			GUIConfigurator.main(args);
			logger.l1("Bad configuration", e);
			return;
		}
		try {
			ORMManager.INSTANSE.start();
		} catch (Exception e) {
			logger.l1("Database init failed, exiting", e);
			System.exit(-1);
		}
		Logger.Loglevel = MainHandler.getCurrentInstance().getIntegerProperty(
				LOG_LEVEL, Logger.LOG_L4);
		{
			File inbound = new File(FtnTools.getInbound());
			if (!(inbound.isDirectory() && inbound.canWrite())) {
				logger.l1("Bad inbound " + inbound.getAbsolutePath());
				System.exit(-1);
			}
		}
		logger.l1(MainHandler.getVersion() + " starting");
		// installer

		new InstallUtil();

		int nThreads = 2 + MainHandler.getCurrentInstance().getIntegerProperty(
				BINKD_THREADS, 10);
		new ThreadPool(nThreads);

		// the  existence check
        ORMManager.get(Echoarea.class);
        ORMManager.get(Echomail.class);
        ORMManager.get(EchomailAwaiting.class);
        ORMManager.get(Filearea.class);
        ORMManager.get(FileForLink.class);
        ORMManager.get(Filemail.class);
        ORMManager.get(FilemailAwaiting.class);
        ORMManager.get(FileSubscription.class);
        ORMManager.get(Jscript.class);
        ORMManager.get(Link.class);
        ORMManager.get(LinkOption.class);
        ORMManager.get(Netmail.class);
        ORMManager.get(Rewrite.class);
        ORMManager.get(Robot.class);
        ORMManager.get(Route.class);
        ORMManager.get(Schedule.class);
        ORMManager.get(ScriptHelper.class);
        ORMManager.get(Subscription.class);
        ORMManager.get(Version.class);

        // eof
		ThreadPool.execute(new BinkpAsyncServer());

		ThreadPool.execute(new BinkpAsyncClientPool());

		Timer mainTimer = new Timer();
		mainTimer
				.schedule(new TimerPoll(), MainHandler.getCurrentInstance()
						.getIntegerProperty(POLL_DELAY, 0) * 1000, MainHandler
						.getCurrentInstance()
						.getIntegerProperty(POLL_PERIOD, 0) * 1000);
		logger.l4("Started TosserTask");
		mainTimer.schedule(new TosserTask(), 10000, 10000);
		logger.l4("Started PollQueueTask");
		mainTimer.schedule(new PollQueueTask(), 10000, 10000);
		logger.l4("Started StatPoster");
		mainTimer.schedule(new NetmailFallback(), 9000, 3600000);
		logger.l4("Started HealthReporter");
		mainTimer.schedule(new HealthReporter(), 60000L, 600000L);
		new StatPoster(mainTimer);
		new JscriptExecutor();
		logger.l4("Started JscriptExecutor");
		{
			if (MainHandler.getCurrentInstance().haveProperty(MODULES)) {
				logger.l1("Starting 3rd party modules");
				String[] modules = MainHandler.getCurrentInstance()
						.getProperty(MODULES, "")
						.replaceAll("[^A-Za-z0-9,\\._:\\\\\\/]", "").split(",");
				for (String module : modules) {
					{
						int idx = module.indexOf(':');
						if (idx < 0) {
							logger.l2("Skipping config string " + module);
							continue;
						}
						String className = module.substring(0, idx);
						String config = module.substring(idx + 1);
						try {
							Class<?> clazz = Class.forName(className);
							final JnodeModule jnodeModule = (JnodeModule) clazz
									.getConstructor(String.class).newInstance(
											config);
							Notifier.INSTANSE.register(SharedModuleEvent.class,
									jnodeModule);
							// module in new thread
							new Thread(new Runnable() {
								@Override
								public void run() {
									jnodeModule.start();
								}
							}).start();
							logger.l2("Module " + className + " started");
						} catch (Exception e) {
							logger.l2("Module " + className + " failed", e);
						}
					}
				}
			}
		}
		logger.l1("jNode is working now");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.l1(MainHandler.getVersion() + " shutdown");
			}
		});
	}

	private static void tryRedirectLog() {
		String redirectMask = MainHandler.getCurrentInstance().getProperty(
				LOGFILE, "");
		if (redirectMask.length() != 0) {
			String zipMask = MainHandler.getCurrentInstance().getProperty(
					LOGZIPPATH, "");
			new Redirector(redirectMask, zipMask).invoke();
		}
	}

	private static final class TosserTask extends TimerTask {
		@Override
		public void run() {
			try {
				TosserQueue.getInstanse().toss();
			} catch (RuntimeException e) {
				logger.l1("Error while tossing", e);
			}
		}

	}

	private static final class PollQueueTask extends TimerTask {

		@Override
		public void run() {
			PollQueue.getSelf().poll();
		}

	}

}
