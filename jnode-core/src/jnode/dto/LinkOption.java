package jnode.dto;

import java.util.HashMap;

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
	public static final String BOOLEAN_IGNORE_PKTPWD = "ignorepktpwd";
	public static final String BOOLEAN_PACK_NETMAIL = "packnetmail";
	public static final String BOOLEAN_PACK_ECHOMAIL = "packechomail";
	public static final String BOOLEAN_CRASH_NETMAIL = "crashnetmail";
	public static final String BOOLEAN_CRASH_ECHOMAIL = "crashechomail";
	public static final String BOOLEAN_CRASH_FILEMAIL = "crashfilemail";
	public static final String BOOLEAN_AUTOCREATE_AREA = "areaautocreate";
	public static final String BOOLEAN_AUTOCREATE_FILE = "fileautocreate";
	public static final String BOOLEAN_POLL_BY_TIMEOT = "pollbytimeout";
	public static final String BOOLEAN_AREAFIX = "areafix";
	public static final String BOOLEAN_FILEFIX = "filefix";
    public static final String BOOLEAN_SCRIPTFIX = "scriptfix";
    public static final String BOOLEAN_PAUSE = "pause";
	public static final String LONG_LINK_LEVEL = "level";
	public static final String SARRAY_LINK_GROUPS = "groups";
	public static final String STRING_AREAFIX_PWD = "areafixpwd";
	public static final String STRING_SCRIPTFIX_PWD = "scriptfixpwd";
	public static final String STRING_FILEFIX_PWD = "filefixpwd";
	public static final String STRING_OUR_AKA = "ouraka";
	private static final HashMap<String, String> options = generateOptionsMap();

	/**
	 * Короткие названия
	 * 
	 * @return
	 */
	private static HashMap<String, String> generateOptionsMap() {
		HashMap<String, String> options = new HashMap<String, String>();
		options.put("ignorepktpwd", "-nopwd");
		options.put("packnetmail", "-netpack");
		options.put("packechomail", "-echpack");
		options.put("crashnetmail", "-netcrash");
		options.put("crashechomail", "-echcrash");
		options.put("crashfilemail", "-fchcrash");
		options.put("areaautocreate", "-aacreate");
		options.put("fileautocreate", "-facreate");
		options.put("pollbytimeout", "-poll");
		options.put("areafix", "-afix");
		options.put("filefix", "-ffix");
		options.put("level", "-lvl");
		options.put("groups", "-grps");
		options.put("areafixpwd", "-afixpwd");
		options.put("filefixpwd", "-ffixpwd");
		options.put("ouraka", "-oaka");
		options.put("pause", "-pause");
		return options;
	}

	public static String getOptByName(String name) {
		return options.get(name);
	}

	public static String getNameByOpt(String opt) {
		String ret = null;
		for (String k : options.keySet()) {
			if (options.get(k).equalsIgnoreCase(opt)) {
				ret = k;
				break;
			}
		}
		return ret;
	}

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
