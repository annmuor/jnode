package jnode.protocol.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 
 * @author kreon
 * 
 */
public class Message {
	private long messageLength;
	private String messageName;
	private InputStream inputStream;

	public Message(File file) throws Exception {
		super();
		messageName = file.getName();
		messageLength = file.length();
		inputStream = new FileInputStream(file);
	}

	public Message(String arg1, long arg2) {
		super();
		this.messageName = arg1;
		this.messageLength = arg2;
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

}
