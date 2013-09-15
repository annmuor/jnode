package jnode.dto;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filemail")
public class Filemail {
	@DatabaseField(generatedId = true)
	private Long id;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "filearea_id")
	private Filearea filearea;
	@DatabaseField(dataType = DataType.LONG_STRING)
	private String filename;
	@DatabaseField(dataType = DataType.LONG_STRING)
	private String filedesc;
	@DatabaseField
	private String filepath;
	@DatabaseField
	private String origin;
	@DatabaseField(dataType = DataType.LONG_STRING)
	private String seenby;
	@DatabaseField(dataType = DataType.LONG_STRING)
	private String path;
	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date created;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Filearea getFilearea() {
		return filearea;
	}

	public void setFilearea(Filearea filearea) {
		this.filearea = filearea;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFiledesc() {
		return filedesc;
	}

	public void setFiledesc(String filedesc) {
		this.filedesc = filedesc;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getSeenby() {
		return seenby;
	}

	public void setSeenby(String seenby) {
		this.seenby = seenby;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}
