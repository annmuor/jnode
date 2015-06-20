package org.jnode.httpd.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "httpd_echoarea_csv")
public class EchoareaCSV {
	@DatabaseField(columnName = "name", id = true)
	private String name;
	@DatabaseField(columnName = "description")
	private String description;
	@DatabaseField(columnName = "num")
	private Long num;
	@DatabaseField(columnName = "latest")
	private Long latest;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getNum() {
		return num;
	}

	public void setNum(Long num) {
		this.num = num;
	}

	public Long getLatest() {
		return latest;
	}

	public void setLatest(Long latest) {
		this.latest = latest;
	}

}