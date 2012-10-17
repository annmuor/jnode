package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filesubscription")
public class FileSubscription {
	@DatabaseField(foreign = true, columnName = "link_id")
	private Link link;
	@DatabaseField(foreign = true, columnName = "filearea_id")
	private Filearea area;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Filearea getArea() {
		return area;
	}

	public void setArea(Filearea area) {
		this.area = area;
	}

}
