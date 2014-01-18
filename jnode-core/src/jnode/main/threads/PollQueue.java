package jnode.main.threads;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import jnode.dto.Link;
import jnode.ftn.types.FtnAddress;

/**
 * 
 * @author kreon
 * 
 */
public class PollQueue {
	private static PollQueue self;
	private HashMap<String, Long> pollMap = new HashMap<>();

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

	public synchronized void end(Link link) {
		String addr = link.getLinkAddress();
		if (addr != null) {
			pollMap.remove(addr);
		}
	}

	public synchronized void end(FtnAddress addr) {
		if (addr != null) {
			pollMap.remove(addr.toString());
		}
	}

	public synchronized boolean isActive(FtnAddress addr) {
		if (addr != null) {
			long now = new Date().getTime();
			Long time = pollMap.get(addr.toString());
			if (time != null) {
				if (now - time < 600000) {
					return true;
				}
			}
		}
		return false;
	}

	public synchronized boolean isActive(Link link) {
		String addr = link.getLinkAddress();
		if (addr != null) {
			long now = new Date().getTime();
			Long time = pollMap.get(addr);
			if (time != null) {
				if (now - time < 600000) {
					return true;
				}
			}
		}
		return false;
	}

	public synchronized void start(FtnAddress addr) {
		if (addr != null) {
			long now = new Date().getTime();
			pollMap.put(addr.toString(), now);
		}
	}

	public synchronized void start(Link link) {
		String addr = link.getLinkAddress();
		if (addr != null) {
			long now = new Date().getTime();
			pollMap.put(addr, now);
		}
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
