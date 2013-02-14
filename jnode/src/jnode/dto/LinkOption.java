package jnode.dto;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Опции для линков
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "linkoptions")
public class LinkOption {
	public static final String BOOLEAN_IGNORE_PKTPWD = "IgnorePktPwd";
	public static final String BOOLEAN_PACK_NETMAIL = "PackNetmail";
	public static final String BOOLEAN_PACK_ECHOMAIL = "PackEchomail";
	public static final String BOOLEAN_CRASH_NETMAIL = "CrashNetmail";
	public static final String BOOLEAN_CRASH_ECHOMAIL = "CrashEchomail";
	public static final String BOOLEAN_CRASH_FILEMAIL = "CrashFilemail";
	public static final String BOOLEAN_AUTOCREATE_AREA = "AreaAutoCreate";
	public static final String BOOLEAN_AUTOCREATE_FILE = "FileAutoCreate";
	public static final String BOOLEAN_POLL_BY_TIMEOT = "PollByTimeout";
	public static final String BOOLEAN_AREAFIX = "AreaFix";
	public static final String BOOLEAN_FILEFIX = "FileFix";
	public static final String LONG_LINK_LEVEL = "Level";
	public static final String SARRAY_LINK_GROUPS = "Groups";
	public static final String STRING_AREAFIX_PWD = "AreaFixPwd";
	public static final String STRING_FILEFIX_PWD = "FileFixPwd";

	@DatabaseField(columnName = "link_id", foreign = true, canBeNull = false, uniqueIndexName = "lopt_idx")
	private Link link;
	@DatabaseField(columnName = "name", canBeNull = false, uniqueIndexName = "lopt_idx")
	private String option;
	@DatabaseField(columnName = "value", canBeNull = false, dataType = DataType.LONG_STRING)
	private String value;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "LinkOptions [link=" + link + ", option=" + option + ", value="
				+ value + "]";
	}

}
