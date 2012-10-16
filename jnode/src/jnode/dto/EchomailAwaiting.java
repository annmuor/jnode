package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "echomailawait")
public class EchomailAwaiting {
	@DatabaseField(foreign = true, columnName = "link_id")
	private Link link;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "echomail_id")
	private Echomail mail;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Echomail getMail() {
		return mail;
	}

	public void setMail(Echomail mail) {
		this.mail = mail;
	}

	public EchomailAwaiting() {
		super();
	}

	public EchomailAwaiting(Link link, Echomail mail) {
		super();
		this.link = link;
		this.mail = mail;
	}

}
