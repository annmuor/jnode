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

package jnode.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public enum Notifier {
	INSTANSE;
	private final Hashtable<Class<? extends IEvent>, List<IEventHandler>> notifyMap;

	private Notifier() {
		notifyMap = new Hashtable<>();
	}

	public void register(Class<? extends IEvent> clazz, IEventHandler handler) {
		if (clazz != null && handler != null) {
			List<IEventHandler> list = notifyMap.get(clazz);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(handler);
			notifyMap.put(clazz, list);
		}
	}

	public void unregister(Class<? extends IEvent> clazz, IEventHandler handler) {
		if (clazz != null && handler != null) {
			List<IEventHandler> list = notifyMap.get(clazz);
			if (list != null) {
				list.remove(handler);
				notifyMap.put(clazz, list);
			}
		}
	}

	public void notify(IEvent event) {
		List<IEventHandler> list = notifyMap.get(event.getClass());
		if (list != null) {
			for (IEventHandler handler : list) {
				if (handler != null) {
					handler.handle(event);
				}
			}
		}
	}
}
