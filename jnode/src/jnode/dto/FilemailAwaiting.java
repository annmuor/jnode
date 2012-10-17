package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filemailawaiting")
public class FilemailAwaiting {
	@DatabaseField(columnName = "link_id", foreign = true, foreignAutoRefresh = true)
	private Link link;
	@DatabaseField(columnName = "filemail_id", foreign = true, foreignAutoRefresh = true)
	private Filemail mail;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Filemail getMail() {
		return mail;
	}

	public void setMail(Filemail mail) {
		this.mail = mail;
	}

	public FilemailAwaiting() {
		super();
	}

	public FilemailAwaiting(Link link, Filemail mail) {
		super();
		this.link = link;
		this.mail = mail;
	}

}
