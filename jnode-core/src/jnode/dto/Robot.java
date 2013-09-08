package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Можно внешних роботов вешать если что - расширяемый интерфейс же ;)
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "robots")
public class Robot {
	@DatabaseField(id = true, columnName = "robot")
	private String robot;
	@DatabaseField(columnName = "className", canBeNull = false)
	private String className;

	public String getRobot() {
		return robot;
	}

	public void setRobot(String robot) {
		this.robot = robot;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
