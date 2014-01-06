package jnode.event;

import jnode.dto.Link;

public class NewFileareaEvent implements IEvent {
	private final String text;

	public NewFileareaEvent(String name, Link link) {
		text = "Filearea " + name + " created by "
				+ ((link == null) ? "local system" : link.getLinkAddress())
				+ "\n";
	}

	@Override
	public String getEvent() {
		return text;
	}

}
