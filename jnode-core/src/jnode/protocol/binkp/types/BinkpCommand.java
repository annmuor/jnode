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

package jnode.protocol.binkp.types;

/**
 * 
 * @author kreon
 * 
 */
public enum BinkpCommand {
	M_NUL(0), M_ADR(1), M_PWD(2), M_FILE(3), M_OK(4), M_EOB(5), M_GOT(6), M_ERR(7), M_BSY(8), M_GET(9), M_SKIP(10), M_PROCESS_FILE(
			99);
	private final int cmd;

	private BinkpCommand(int cmd) {
		this.cmd = cmd;
	}

	public int getCmd() {
		return cmd;
	}

	@Override
	public String toString() {
		return String.format("%s", this.name());
	}

}
