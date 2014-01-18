package jnode.protocol.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * 
 * @author kreon
 * 
 */
public class Message {
	private long messageLength;
	private String messageName;
	private InputStream inputStream;
	private boolean secure = true;
	private Long unixtime = new Date().getTime();
	private File file;

	public Message(File file) throws Exception {
		super();
		this.file = file;
		messageName = file.getName();
		messageLength = file.length();
		inputStream = new FileInputStream(file);
	}

	public Message(String name, long len) {
		super();
		this.messageName = name;
		this.messageLength = len;
	}

	public void delete() {
		if (file != null) {
			file.delete();
		}
	}

	public long getMessageLength() {
		return messageLength;
	}

	public void setMessageLength(long arg) {
		this.messageLength = arg;
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String arg) {
		this.messageName = arg;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream arg) {
		this.inputStream = arg;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public Long getUnixtime() {
		return unixtime;
	}

	public void setUnixtime(Long unixtime) {
		this.unixtime = unixtime;
	}
}
