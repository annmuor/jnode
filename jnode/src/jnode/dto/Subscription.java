package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "subscription")
public class Subscription {
	@DatabaseField(columnName = "link_id", foreign = true, uniqueIndexName = "subs_idx")
	private Link link;
	@DatabaseField(columnName = "echoarea_id", foreign = true, uniqueIndexName = "subs_idx")
	private Echoarea area;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Echoarea getArea() {
		return area;
	}

	public void setArea(Echoarea area) {
		this.area = area;
	}

}
