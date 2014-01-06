package jnode.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public enum Notifier {
	INSTANSE;
	private final Hashtable<Class<? extends IEvent>, List<IEventHandler>> notifyMap;

	private Notifier() {
		notifyMap = new Hashtable<Class<? extends IEvent>, List<IEventHandler>>();
	}

	public void register(Class<? extends IEvent> clazz, IEventHandler handler) {
		if (clazz != null && handler != null) {
			List<IEventHandler> list = notifyMap.get(clazz);
			if (list == null) {
				list = new ArrayList<IEventHandler>();
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
