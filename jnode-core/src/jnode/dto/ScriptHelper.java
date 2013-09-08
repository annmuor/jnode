package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Хелперы - для внешних модулей
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "scripthelpers")
public class ScriptHelper {
	@DatabaseField(id = true, columnName = "helper")
	private String id;
	@DatabaseField(columnName = "className", canBeNull = false)
	private String className;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
