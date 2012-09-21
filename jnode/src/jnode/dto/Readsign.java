package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "readsing")
public class Readsign {
	@DatabaseField(foreign = true, columnName = "link_id", index = true)
	private Link link;
	@DatabaseField(foreign = true, columnName = "echomail_id", index = true)
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

	public Readsign() {
		super();
	}

	public Readsign(Link link, Echomail mail) {
		super();
		this.link = link;
		this.mail = mail;
	}

}
