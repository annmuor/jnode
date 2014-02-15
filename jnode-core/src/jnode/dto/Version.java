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

import jnode.ftn.FtnTools;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "version")
public class Version {
	@DatabaseField(columnName = "maj_ver")
	private Long majorVersion;
	@DatabaseField(columnName = "min_ver")
	private Long minorVersion;
	@DatabaseField(columnName = "int_at", dataType = DataType.DATE_LONG)
	private Date installedAt;

	public Long getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(Long majorVersion) {
		this.majorVersion = majorVersion;
	}

	public Long getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(Long minorVersion) {
		this.minorVersion = minorVersion;
	}

	public Date getInstalledAt() {
		return installedAt;
	}

	public void setInstalledAt(Date installedAt) {
		this.installedAt = installedAt;
	}

	@Override
	public String toString() {
		return String.format("%d.%d @ %s", majorVersion, minorVersion,
				FtnTools.FORMAT.format(installedAt));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((majorVersion == null) ? 0 : majorVersion.hashCode());
		result = prime * result
				+ ((minorVersion == null) ? 0 : minorVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (majorVersion == null) {
			if (other.majorVersion != null)
				return false;
		} else if (!majorVersion.equals(other.majorVersion))
			return false;
		if (minorVersion == null) {
			if (other.minorVersion != null)
				return false;
		} else if (!minorVersion.equals(other.minorVersion))
			return false;
		return true;
	}
	
	public boolean equals(String ver) {
		if (ver != null) {
			String[] vera = ver.split("\\.");
			if (vera.length == 2) {
				try {
					if (Long.valueOf(vera[0]).longValue() == getMajorVersion()
							.longValue()
							&& Long.valueOf(vera[1]).longValue() == getMinorVersion()
									.longValue()) {
						return true;
					}
				} catch (NumberFormatException ignore) {
				}
			}
		}
		return false;
	}
	

}
