package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filearea")
public class Filearea {
	@DatabaseField(generatedId = true)
	private Long id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String description;
	@DatabaseField(columnName = "wlevel", canBeNull = false, defaultValue = "0")
	private Long writelevel;
	@DatabaseField(columnName = "rlevel", canBeNull = false, defaultValue = "0")
	private Long readlevel;
	@DatabaseField(columnName = "grp", canBeNull = false, defaultValue = "")
	private String group;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Long getWritelevel() {
		return writelevel;
	}

	public void setWritelevel(Long writelevel) {
		this.writelevel = writelevel;
	}

	public Long getReadlevel() {
		return readlevel;
	}

	public void setReadlevel(Long readlevel) {
		this.readlevel = readlevel;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
