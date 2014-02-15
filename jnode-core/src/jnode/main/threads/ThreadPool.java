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

import java.util.LinkedList;

import jnode.logger.Logger;

public class ThreadPool {
	private static ThreadPool self;
	private static final Logger logger = Logger.getLogger(ThreadPool.class);
	private final ThreadRunner[] threads;
	private final LinkedList<Runnable> queue;

	public ThreadPool(int numThreads) {
		queue = new LinkedList<>();
		threads = new ThreadRunner[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new ThreadRunner();
			threads[i].start();
		}
		logger.l3("Thread pool (" + numThreads + " threads) started");
		self = this;
	}

	public static void execute(Runnable r) {
		if (self != null) {
			synchronized (self.queue) {
				logger.l5("+ 1 task to queue");
				self.queue.addLast(r);
				self.queue.notify();
			}
		}
	}

	public static boolean isBusy() {
		if (self != null) {
			return (self.queue.size() > self.threads.length);
		}
		return true;
	}

	private class ThreadRunner extends Thread {
		public ThreadRunner() {
			logger.l5("Created ThreadRunner id " + getId());
		}

		@Override
		public void run() {
			Runnable r;
			while (true) {
				synchronized (queue) {
					while (queue.isEmpty()) {
						try {
							queue.wait();
						} catch (InterruptedException ignore) {
						}
					}
					r = queue.removeFirst();
				}
				try {
					r.run();
				} catch (RuntimeException e) {
					logger.l1("Runtime exception in thread " + getId(), e);
				}
			}

		}

	}
}
