package jnode.dto;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "echomail")
public class Echomail {
	@DatabaseField(generatedId = true, columnName = "id")
	private Long id;
	@DatabaseField(columnName = "echoarea_id", foreign = true, foreignAutoRefresh = true)
	private Echoarea area;
	@DatabaseField(columnName = "from_name", canBeNull = false)
	private String fromName;
	@DatabaseField(columnName = "to_name", canBeNull = false)
	private String toName;
	@DatabaseField(columnName = "from_ftn_addr", canBeNull = false)
	private String fromFTN;
	@DatabaseField(columnName = "date", dataType = DataType.DATE_LONG)
	private Date date;
	@DatabaseField(columnName = "subject", dataType = DataType.LONG_STRING)
	private String subject;
	@DatabaseField(columnName = "message", dataType = DataType.LONG_STRING)
	private String text;
	@DatabaseField(columnName = "seen_by", dataType = DataType.LONG_STRING)
	private String seenBy;
	@DatabaseField(columnName = "path", dataType = DataType.LONG_STRING)
	private String path;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Echoarea getArea() {
		return area;
	}

	public void setArea(Echoarea area) {
		this.area = area;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public String getFromFTN() {
		return fromFTN;
	}

	public void setFromFTN(String fromFTN) {
		this.fromFTN = fromFTN;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSeenBy() {
		return seenBy;
	}

	public void setSeenBy(String seenBy) {
		this.seenBy = seenBy;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
