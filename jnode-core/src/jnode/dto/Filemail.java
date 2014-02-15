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
