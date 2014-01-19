package jnode.main.threads;

import java.util.LinkedList;

import jnode.logger.Logger;

public class ThreadPool {
	private static ThreadPool self;
	private static final Logger logger = Logger.getLogger(ThreadPool.class);
	private final ThreadRunner[] threads;
	private final LinkedList<Runnable> queue;

	public ThreadPool(int numThreads) {
		queue = new LinkedList<Runnable>();
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
