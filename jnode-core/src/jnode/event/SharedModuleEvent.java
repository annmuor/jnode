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

import java.util.HashMap;
import java.util.Map;

public class SharedModuleEvent implements IEvent {
	private String to;
	private String from;
	private Map<String, Object> params;

	public SharedModuleEvent(String to, Object... args) {
		try {
			throw new Exception();
		} catch (Exception e) {
			from = e.getStackTrace()[1].getClassName();
		}
		this.to = to;
		this.params = new HashMap<String, Object>();
		if (args.length % 2 == 0) {
			for (int i = 0; i < args.length; i += 2) {
				Object var = args[i];
				Object value = args[i + 1];
				params.put(var.toString(), value);
			}
		}
	}

	@Override
	public String getEvent() {
		return "";
	}

	public String to() {
		return to;
	}

	public Map<String, Object> params() {
		return params;
	}

	public String from() {
		return from;
	}

	@Override
	public String toString() {
		return String.format("SharedModulesEvent{%s->%s, %d}", from, to,
				params.size());
	}
}
