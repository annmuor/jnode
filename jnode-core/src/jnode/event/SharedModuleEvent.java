package jnode.event;

import java.util.HashMap;
import java.util.Map;

public class SharedModuleEvent implements IEvent {
	private String to;
	private String from;
	private Map<String, Object> params;

	public SharedModuleEvent(String to, Object... args) {
		try {
			throw new Exception();
		} catch (Exception e) {
			from = e.getStackTrace()[1].getClassName();
		}
		this.to = to;
		this.params = new HashMap<String, Object>();
		if (args.length % 2 == 0) {
			for (int i = 0; i < args.length; i += 2) {
				Object var = args[i];
				Object value = args[i + 1];
				params.put(var.toString(), value);
			}
		}
	}

	@Override
	public String getEvent() {
		return "";
	}

	public String to() {
		return to;
	}

	public Map<String, Object> params() {
		return params;
	}

	public String from() {
		return from;
	}

	@Override
	public String toString() {
		return String.format("SharedModulesEvent{%s->%s, %d}", from, to,
				params.size());
	}
}
