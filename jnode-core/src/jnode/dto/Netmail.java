package jnode.dto;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "netmail")
public class Netmail {
	@DatabaseField(columnName = "id", generatedId = true)
	private Long id;
	@DatabaseField(columnName = "from_name")
	private String fromName;
	@DatabaseField(columnName = "to_name")
	private String toName;
	@DatabaseField(columnName = "from_address", canBeNull = false)
	private String fromFTN;
	@DatabaseField(columnName = "to_address", canBeNull = false)
	private String toFTN;
	@DatabaseField(columnName = "subject")
	private String subject;
	@DatabaseField(columnName = "text", dataType = DataType.LONG_STRING)
	private String text;
	@DatabaseField(columnName = "date", dataType = DataType.DATE_LONG)
	private Date date;
	@DatabaseField(columnName = "route_via", foreign = true, foreignAutoRefresh = true, index = true)
	private Link routeVia;
	@DatabaseField(columnName = "send", index = true, canBeNull = false, defaultValue = "false", dataType = DataType.BOOLEAN)
	private boolean send;
	@DatabaseField(columnName = "attr", canBeNull = false, defaultValue = "256", dataType = DataType.INTEGER)
	private int attr;
	@DatabaseField(columnName="lastModified",dataType=DataType.DATE_LONG)
	private Date lastModified = new Date();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public String getFromFTN() {
		return fromFTN;
	}

	public void setFromFTN(String fromFTN) {
		this.fromFTN = fromFTN;
	}

	public String getToFTN() {
		return toFTN;
	}

	public void setToFTN(String toFTN) {
		this.toFTN = toFTN;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Link getRouteVia() {
		return routeVia;
	}

	public void setRouteVia(Link routeVia) {
		this.routeVia = routeVia;
	}

	public boolean isSend() {
		return send;
	}

	public void setSend(boolean send) {
		this.send = send;
	}

	public int getAttr() {
		return attr;
	}

	public void setAttr(int attr) {
		this.attr = attr;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return "Netmail [id=" + id + ", fromName=" + fromName + ", toName="
				+ toName + ", fromFTN=" + fromFTN + ", toFTN=" + toFTN
				+ ", subject=" + subject + ", text=" + text + ", date=" + date
				+ ", routeVia=" + routeVia + "]";
	}

}
