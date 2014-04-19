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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jnode.logger.Logger;

public class ThreadPool {
	private static ThreadPool self;
	private static final Logger logger = Logger.getLogger(ThreadPool.class);
	private ThreadPoolExecutor executor;

	public ThreadPool(int numThreads) {
		executor = new ThreadPoolExecutor(numThreads, (int) (numThreads * 1.5),
				30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		logger.l3("Thread pool (" + numThreads + " threads) started");
		self = this;
	}

	public static void execute(Runnable r) {
		if (self != null) {
			self.executor.execute(r);
		}
	}

	public static boolean isBusy() {
		if (self != null) {
			return self.executor.getQueue().size() > self.executor
					.getMaximumPoolSize();
		}
		return true;
	}
}
