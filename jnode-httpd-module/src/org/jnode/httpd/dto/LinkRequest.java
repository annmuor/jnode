package org.jnode.httpd.dto;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "httpd_link_request")
public class LinkRequest {
	@DatabaseField(id = true)
	private String id = UUID.randomUUID().toString();
	@DatabaseField
	private String name;
	@DatabaseField
	private String address;
	@DatabaseField
	private String host;
	@DatabaseField
	private Integer port;
	@DatabaseField
	private String akey;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getAkey() {
		return akey;
	}

	public void setAkey(String akey) {
		this.akey = akey;
	}

}
