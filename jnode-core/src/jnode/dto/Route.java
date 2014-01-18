package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "routing")
public class Route {
	@DatabaseField(generatedId = true)
	private Long id;
	@DatabaseField(columnName = "nice")
	private Long nice;
	@DatabaseField(columnName = "from_name", defaultValue = "*")
	private String fromName;
	@DatabaseField(columnName = "to_name", defaultValue = "*")
	private String toName;
	@DatabaseField(columnName = "from_address", defaultValue = "*")
	private String fromAddr;
	@DatabaseField(columnName = "to_address", defaultValue = "*")
	private String toAddr;
	@DatabaseField(columnName = "subject", defaultValue = "*")
	private String subject;
	@DatabaseField(columnName = "route_via", foreign = true)
	private Link routeVia;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getNice() {
		return nice;
	}

	public void setNice(Long nice) {
		this.nice = nice;
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

	public String getFromAddr() {
		return fromAddr;
	}

	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}

	public String getToAddr() {
		return toAddr;
	}

	public void setToAddr(String toAddr) {
		this.toAddr = toAddr;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Link getRouteVia() {
		return routeVia;
	}

	public void setRouteVia(Link routeVia) {
		this.routeVia = routeVia;
	}

}
