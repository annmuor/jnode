package jnode.main.threads;

import java.util.LinkedList;

import jnode.dto.Link;

/**
 * 
 * @author kreon
 * 
 */
public class PollQueue {
	private static PollQueue self;

	public static PollQueue getSelf() {
		if (self == null) {
			synchronized (PollQueue.class) {
				self = new PollQueue();
			}
		}
		return self;
	}

	private LinkedList<Link> queue = new LinkedList<Link>();

	public synchronized void add(Link link) {
		if (link.getProtocolPort() > 0 && !"-".equals(link.getProtocolHost())) {
			if (!queue.contains(link)) {
				queue.addLast(link);
				this.notify();
			}
		}
	}

	public synchronized Link getNext() {
		return queue.removeFirst();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
