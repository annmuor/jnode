package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "links")
public class Link {
	@DatabaseField(columnName = "id", generatedId = true)
	private Long id;
	@DatabaseField(columnName = "station_name", canBeNull = false)
	private String linkName;
	@DatabaseField(columnName = "ftn_address", uniqueIndex = true, canBeNull = false)
	private String linkAddress;
	@DatabaseField(columnName = "pkt_password", defaultValue = "", canBeNull = false)
	private String paketPassword;
	@DatabaseField(columnName = "password", defaultValue = "-", canBeNull = false)
	private String protocolPassword;
	@DatabaseField(columnName = "host")
	private String protocolHost;
	@DatabaseField(columnName = "port")
	private Integer protocolPort;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getLinkAddress() {
		return linkAddress;
	}

	public void setLinkAddress(String linkAddress) {
		this.linkAddress = linkAddress;
	}

	public String getPaketPassword() {
		return paketPassword;
	}

	public void setPaketPassword(String paketPassword) {
		this.paketPassword = paketPassword;
	}

	public String getProtocolPassword() {
		return protocolPassword;
	}

	public void setProtocolPassword(String protocolPassword) {
		this.protocolPassword = protocolPassword;
	}

	public String getProtocolHost() {
		return protocolHost;
	}

	public void setProtocolHost(String protocolHost) {
		this.protocolHost = protocolHost;
	}

	public Integer getProtocolPort() {
		return protocolPort;
	}

	public void setProtocolPort(Integer protocolPort) {
		this.protocolPort = protocolPort;
	}

	@Override
	public String toString() {
		return "Link [id=" + id + ", linkName=" + linkName + ", linkAddress="
				+ linkAddress + ", paketPassword=" + paketPassword
				+ ", protocolPassword=" + protocolPassword + ", protocolHost="
				+ protocolHost + ", protocolPort=" + protocolPort + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((linkAddress == null) ? 0 : linkAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (linkAddress == null) {
			if (other.linkAddress != null)
				return false;
		} else if (!linkAddress.equals(other.linkAddress))
			return false;
		return true;
	}

}
