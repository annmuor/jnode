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

package jnode.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filemailawaiting")
public class FilemailAwaiting {
	@DatabaseField(columnName = "link_id", foreign = true, foreignAutoRefresh = true)
	private Link link;
	@DatabaseField(columnName = "filemail_id", foreign = true, foreignAutoRefresh = true)
	private Filemail mail;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Filemail getMail() {
		return mail;
	}

	public void setMail(Filemail mail) {
		this.mail = mail;
	}

	public FilemailAwaiting() {
		super();
	}

	public FilemailAwaiting(Link link, Filemail mail) {
		super();
		this.link = link;
		this.mail = mail;
	}

}
