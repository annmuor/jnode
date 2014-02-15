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
		if (link.getProtocolAddress() != null
				&& !link.getProtocolAddress().isEmpty()
				&& !"-".equals(link.getProtocolAddress())) {
			if (!queue.contains(link) && !isActive(link)) {
				queue.addLast(link);
			}
		}
	}

	public synchronized void poll() {
		for (int i = 0; i < queue.size(); i++) {
			this.notify();
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
