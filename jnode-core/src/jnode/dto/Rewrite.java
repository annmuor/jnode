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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "rewrite")
public class Rewrite {
	public static enum Type {
		NETMAIL, ECHOMAIL
	}

	@DatabaseField(generatedId = true)
	private Long id;
	@DatabaseField(canBeNull = false, defaultValue = "0", columnName = "nice")
	private Long nice;
	@DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false, index = true, columnName = "type")
	private Type type;
	@DatabaseField(dataType = DataType.BOOLEAN, canBeNull = false, defaultValue = "false")
	private boolean last;
	@DatabaseField(columnName = "ofa", defaultValue = "*")
	private String orig_from_addr;
	@DatabaseField(columnName = "ota", defaultValue = "*")
	private String orig_to_addr;
	@DatabaseField(columnName = "ofn", defaultValue = "*")
	private String orig_from_name;
	@DatabaseField(columnName = "otn", defaultValue = "*")
	private String orig_to_name;
	@DatabaseField(columnName = "os", defaultValue = "*")
	private String orig_subject;
	@DatabaseField(columnName = "nfa", defaultValue = "*")
	private String new_from_addr;
	@DatabaseField(columnName = "nta", defaultValue = "*")
	private String new_to_addr;
	@DatabaseField(columnName = "nfn", defaultValue = "*")
	private String new_from_name;
	@DatabaseField(columnName = "ntn", defaultValue = "*")
	private String new_to_name;
	@DatabaseField(columnName = "ns", defaultValue = "*")
	private String new_subject;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getOrig_from_addr() {
		return orig_from_addr;
	}

	public void setOrig_from_addr(String orig_from_addr) {
		this.orig_from_addr = orig_from_addr;
	}

	public String getOrig_to_addr() {
		return orig_to_addr;
	}

	public void setOrig_to_addr(String orig_to_addr) {
		this.orig_to_addr = orig_to_addr;
	}

	public String getOrig_from_name() {
		return orig_from_name;
	}

	public void setOrig_from_name(String orig_from_name) {
		this.orig_from_name = orig_from_name;
	}

	public String getOrig_to_name() {
		return orig_to_name;
	}

	public void setOrig_to_name(String orig_to_name) {
		this.orig_to_name = orig_to_name;
	}

	public String getOrig_subject() {
		return orig_subject;
	}

	public void setOrig_subject(String orig_subject) {
		this.orig_subject = orig_subject;
	}

	public String getNew_from_addr() {
		return new_from_addr;
	}

	public void setNew_from_addr(String new_from_addr) {
		this.new_from_addr = new_from_addr;
	}

	public String getNew_to_addr() {
		return new_to_addr;
	}

	public void setNew_to_addr(String new_to_addr) {
		this.new_to_addr = new_to_addr;
	}

	public String getNew_from_name() {
		return new_from_name;
	}

	public void setNew_from_name(String new_from_name) {
		this.new_from_name = new_from_name;
	}

	public String getNew_to_name() {
		return new_to_name;
	}

	public void setNew_to_name(String new_to_name) {
		this.new_to_name = new_to_name;
	}

	public String getNew_subject() {
		return new_subject;
	}

	public void setNew_subject(String new_subject) {
		this.new_subject = new_subject;
	}

	public Long getNice() {
		return nice;
	}

	public void setNice(Long nice) {
		this.nice = nice;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

}
