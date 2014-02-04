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

import java.io.Serializable;

import jnode.ftn.types.FtnAddress;

class NodelistIndex implements Serializable {
	private static final long serialVersionUID = 1L;
	private FtnNdlAddress[] nodelist;
	private Long timestamp;

	public FtnNdlAddress exists(FtnAddress address) {
		FtnAddress addr = address.clone();
		addr.setPoint(0);
		for (FtnNdlAddress a : nodelist) {
			if (a.equals(addr)) {
				return a;
			}
		}
		return null;
	}

	public NodelistIndex() {

	}

	public NodelistIndex(FtnNdlAddress[] nodelist, Long timestamp) {
		super();
		this.nodelist = nodelist;
		this.timestamp = timestamp;
	}

	public Long getTimestamp() {
		return timestamp;
	}

}
