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

package jnode.ndl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.ftn.types.FtnAddress;

public class FtnNdlAddress extends FtnAddress {
	private static final Pattern IBN_PATTERN = Pattern.compile(
			".*,IBN(:[^,]+)?.*", Pattern.CASE_INSENSITIVE);
	private static final Pattern INA_PATTERN = Pattern.compile(
			".*,INA(:[^,]+)?.*", Pattern.CASE_INSENSITIVE);

	public static enum Status {
		HOLD, DOWN, HUB, HOST, PVT, NORMAL
	}

	private static final long serialVersionUID = 1L;
	private Status status;

	private String line;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public FtnNdlAddress(String addr, Status status) {
		super(addr);
		this.status = status;
	}

	public FtnNdlAddress(String addr) {
		super(addr);
		this.status = Status.NORMAL;
	}

	public FtnNdlAddress(FtnAddress address) {
		this.zone = address.getZone();
		this.net = address.getNet();
		this.node = address.getNode();
		this.point = address.getPoint();
		this.status = Status.NORMAL;
	}

	public FtnNdlAddress(Status status) {
		super();
		this.status = status;
	}

	public FtnNdlAddress() {
		super();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (!(object instanceof FtnAddress)) {
			return false;
		}
		return object.equals(this);
	}

	@Override
	public String toString() {
		return "FtnNdlAddress [" + super.toString() + ", status=" + status
				+ "]";
	}

	public int getBinkpPort() {
		if (line != null) {
			Matcher m = IBN_PATTERN.matcher(line);
			if (m.matches()) {
				if (m.group(1) != null) {
					try {
						String[] r = m.group(1).split(":");
						return Integer.valueOf(r[r.length - 1]);
					} catch (NumberFormatException e) {
						return 24554;
					}
				} else {
					return 24554;
				}
			}
		}
		return -1;
	}

	public String getInetHost() {
		if (line != null) {
			Matcher m = INA_PATTERN.matcher(line);
			if (m.matches()) {
				if (m.group(1) != null) {
					return m.group(1).substring(1);
				} else {
					return "-";
				}
			}
		}
		return null;
	}
}
