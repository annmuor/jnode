package jnode.event;

import jnode.dto.Link;

public class NewEchoareaEvent implements IEvent {
	private final String text;

	public NewEchoareaEvent(String name, Link link) {
		text = "Echoarea " + name + " created by "
				+ ((link == null) ? "local system" : link.getLinkAddress())
				+ "\n";
	}

	@Override
	public String getEvent() {
		return text;
	}

}
