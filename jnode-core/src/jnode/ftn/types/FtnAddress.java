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

package jnode.ftn.types;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.main.MainHandler;

/**
 * 
 * @author kreon
 * 
 */
public class FtnAddress implements Serializable {

	private static final long serialVersionUID = 1L;
	protected int zone;
	protected int net;
	protected int node;
	protected int point;
	public static final Pattern fidonetAddress = Pattern
			.compile("^(\\d)?:?(\\d{1,5})/(\\d{1,5})\\.?(\\d{1,5})?@?(\\S+)?$");

	public FtnAddress(String addr) throws NumberFormatException {

		Matcher m = fidonetAddress.matcher(addr);
		if (m.matches()) {
			if (m.group(1) != null && m.group(1).length() > 0) {
				zone = new Integer(m.group(1));
			} else {
				zone = MainHandler.getCurrentInstance().getInfo().getZone();
			}
			net = new Integer(m.group(2));
			node = new Integer(m.group(3));
			if (m.group(4) != null && m.group(4).length() > 0) {
				point = new Integer(m.group(4));
			} else {
				point = 0;
			}
		} else {
			throw new NumberFormatException(addr + " is invalid ftn address");
		}
	}

	public FtnAddress(int zone, int net, int node, int point) {
		this.zone = zone;
		this.point = point;
		this.node = node;
		this.net = net;
	}

	public FtnAddress() {
		zone = MainHandler.getCurrentInstance().getInfo().getZone();
		net = 0;
		node = 0;
		point = 0;
	}

	@Override
	public String toString() {
		return (point > 0) ? String.format("%d:%d/%d.%d", zone, net, node,
				point) : String.format("%d:%d/%d", zone, net, node);
	}

	public String intl() {
		return String.format("%d:%d/%d", zone, net, node);
	}

	public String topt() {
		if (point != 0) {
			return String.format("\001TOPT %d\r", point);
		} else {
			return "";
		}
	}

	public String fmpt() {
		if (point != 0) {
			return String.format("\001FMPT %d\r", point);
		} else {
			return "";
		}
	}

	public short getZone() {
		return (short) zone;
	}

	public short getNet() {
		return (short) net;
	}

	public short getNode() {
		return (short) node;
	}

	public short getPoint() {
		return (short) point;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public void setNet(int net) {
		this.net = net;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + net;
		result = prime * result + node;
		result = prime * result + point;
		result = prime * result + zone;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FtnAddress))
			return false;
		FtnAddress other = (FtnAddress) obj;
		if (net != other.net)
			return false;
		if (node != other.node)
			return false;
		if (point != other.point)
			return false;
		if (zone != other.zone)
			return false;
		return true;
	}

	public boolean isPointOf(FtnAddress boss) {
		if (boss.zone == this.zone) {
			if (boss.net == this.net) {
				if (boss.node == this.node) {
					return true;
				}
			}
		}
		return false;
	}

	public FtnAddress clone() {
		return new FtnAddress(zone, net, node, point);
	}

	public FtnAddress cloneNode() {
		return new FtnAddress(zone, net, node, 0);
	}

}
