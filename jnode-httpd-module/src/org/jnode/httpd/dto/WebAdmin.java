package org.jnode.httpd.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="httpd_webadmin")
public class WebAdmin {
	@DatabaseField(generatedId = true, columnName = "id")
	private Long id;
	@DatabaseField(columnName = "username", unique = true, canBeNull = false)
	private String username;
	@DatabaseField(columnName = "password", canBeNull = false)
	private String password;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
