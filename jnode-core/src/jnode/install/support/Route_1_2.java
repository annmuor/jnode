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

package jnode.install.support;

import jnode.dto.Link;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author kreon
 * 
 */
@DatabaseTable(tableName = "routing")
public class Route_1_2 {
	@DatabaseField(columnName = "nice")
	private Long nice;
	@DatabaseField(columnName = "from_name", defaultValue = "*")
	private String fromName;
	@DatabaseField(columnName = "to_name", defaultValue = "*")
	private String toName;
	@DatabaseField(columnName = "from_address", defaultValue = "*")
	private String fromAddr;
	@DatabaseField(columnName = "to_address", defaultValue = "*")
	private String toAddr;
	@DatabaseField(columnName = "subject", defaultValue = "*")
	private String subject;
	@DatabaseField(columnName = "route_via", foreign = true)
	private Link routeVia;

	public Long getNice() {
		return nice;
	}

	public void setNice(Long nice) {
		this.nice = nice;
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

	public String getFromAddr() {
		return fromAddr;
	}

	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}

	public String getToAddr() {
		return toAddr;
	}

	public void setToAddr(String toAddr) {
		this.toAddr = toAddr;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Link getRouteVia() {
		return routeVia;
	}

	public void setRouteVia(Link routeVia) {
		this.routeVia = routeVia;
	}

}
