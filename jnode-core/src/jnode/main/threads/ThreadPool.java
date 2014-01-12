package jnode.main.threads;

import java.util.LinkedList;

import jnode.logger.Logger;

public class ThreadPool {
	private static ThreadPool self;
	private static final Logger logger = Logger.getLogger(ThreadPool.class);
	private ThreadRunner[] threads;
	private LinkedList<Runnable> queue;

	public ThreadPool(int numThreads) {
		threads = new ThreadRunner[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new ThreadRunner();
			threads[i].start();
		}
		queue = new LinkedList<>();
		logger.l1("Thread pool (" + numThreads + " threads) started");
		self = this;
	}

	public static void execute(Runnable r) {
		if (self != null) {
			synchronized (self.queue) {
				self.queue.addLast(r);
				self.queue.notify();
			}
		}
	}

	private class ThreadRunner extends Thread {

		@Override
		public void run() {
			while (true) {
				synchronized (queue) {
					while (queue.isEmpty()) {
						try {
							queue.wait();
						} catch (InterruptedException ignore) {
						}
					}
					try {
						Runnable r = queue.removeFirst();
						r.run();
					} catch (RuntimeException e) {
						logger.l1("Runtime exception in thread " + getId(), e);
					}
				}
			}

		}

	}
}
