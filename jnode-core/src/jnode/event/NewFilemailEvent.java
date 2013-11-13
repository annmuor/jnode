package jnode.event;

import jnode.dto.Filemail;

public class NewFilemailEvent implements IEvent {
	private final Filemail filemail;

	@Override
	public String getEvent() {
		return "";
	}

	public Filemail getFilemail() {
		return filemail;
	}

	public NewFilemailEvent(Filemail filemail) {
		super();
		this.filemail = filemail;
	}

}
