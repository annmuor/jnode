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

package jnode.main;

import java.util.ArrayList;
import java.util.List;

import jnode.ftn.types.FtnAddress;

/**
 * 
 * @author kreon
 * 
 */
public final class SystemInfo {
	private static final String INFO_SYSOP = "info.sysop";
	private static final String INFO_LOCATION = "info.location";
	private static final String INFO_STATIONNAME = "info.stationname";
	private static final String INFO_NDL = "info.ndl";
	private static final String INFO_ADDRESS = "info.address";
	private static final String INFO_DEFAULT_ZONE = "info.zone";
	private final String sysop;
	private final String location;
	private final String stationName;
	private final List<FtnAddress> addressList;
	private final Integer zone;
	private final String NDL;

	public SystemInfo(MainHandler handler) {
		sysop = handler.getProperty(INFO_SYSOP, "Nobody");
		location = handler.getProperty(INFO_LOCATION, "Nowhere");
		stationName = handler.getProperty(INFO_STATIONNAME, "Noname");
		NDL = handler.getProperty(INFO_NDL, "MO,TCP,BINKP");
		zone = new Integer(handler.getProperty(INFO_DEFAULT_ZONE, "2"));

		String[] addra = handler.getProperty(INFO_ADDRESS, "2:9999/9999")
				.replaceAll("[^\\/0-9,:\\.]", "").split(",");
		addressList = new ArrayList<>();
		for (String address : addra) {
			addressList.add(new FtnAddress(address));
		}

	}

	public String getSysop() {
		return sysop;
	}

	public String getLocation() {
		return location;
	}

	public String getStationName() {
		return stationName;
	}

	public List<FtnAddress> getAddressList() {
		return addressList;
	}

	public String getNDL() {
		return NDL;
	}

	public Integer getZone() {
		return zone;
	}
}