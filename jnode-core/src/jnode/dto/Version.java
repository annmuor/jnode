package jnode.dto;

import java.util.Date;

import jnode.ftn.FtnTools;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "version")
public class Version {
	@DatabaseField(columnName = "maj_ver")
	private Long majorVersion;
	@DatabaseField(columnName = "min_ver")
	private Long minorVersion;
	@DatabaseField(columnName = "int_at", dataType = DataType.DATE_LONG)
	private Date installedAt;

	public Long getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(Long majorVersion) {
		this.majorVersion = majorVersion;
	}

	public Long getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(Long minorVersion) {
		this.minorVersion = minorVersion;
	}

	public Date getInstalledAt() {
		return installedAt;
	}

	public void setInstalledAt(Date installedAt) {
		this.installedAt = installedAt;
	}

	@Override
	public String toString() {
		return String.format("%d.%d @ %s", majorVersion, minorVersion,
				FtnTools.FORMAT.format(installedAt));
	}

}
