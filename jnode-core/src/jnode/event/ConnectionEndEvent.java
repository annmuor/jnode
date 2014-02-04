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

package jnode.event;

import jnode.ftn.types.FtnAddress;

public class ConnectionEndEvent implements IEvent {
	private int bytesReceived;
	private int bytesSended;
	private FtnAddress address;
	private boolean incoming;
	private boolean success;

	@Override
	public String getEvent() {
		return "";
	}

	public ConnectionEndEvent() {
		super();
	}

	public ConnectionEndEvent(FtnAddress address, boolean incoming,
			boolean success, int bytesReceived, int bytesSended) {
		super();
		this.bytesReceived = bytesReceived;
		this.bytesSended = bytesSended;
		this.address = address;
		this.incoming = incoming;
		this.success = success;
	}

	public ConnectionEndEvent(boolean incoming, boolean success) {
		super();
		this.incoming = incoming;
		this.success = success;
		bytesReceived = 0;
		bytesSended = 0;
		address = null;
	}

	public int getBytesReceived() {
		return bytesReceived;
	}

	public int getBytesSended() {
		return bytesSended;
	}

	public FtnAddress getAddress() {
		return address;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public boolean isSuccess() {
		return success;
	}

	@Override
	public String toString() {
		return "ConnectionEndEvent [bytesReceived=" + bytesReceived
				+ ", bytesSended=" + bytesSended + ", address=" + address
				+ ", incoming=" + incoming + ", success=" + success + "]";
	}

}
