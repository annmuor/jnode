/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.protocol.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	private Long unixtime = new Date().getTime() / 1000L;
	private File file;

	public Message(File file) throws IOException {
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
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("messageLength=").append(messageLength);
        sb.append(", messageName='").append(messageName).append('\'');
        sb.append(", secure=").append(secure);
        sb.append(", unixtime=").append(unixtime);
        sb.append('}');
        return sb.toString();
    }
}
