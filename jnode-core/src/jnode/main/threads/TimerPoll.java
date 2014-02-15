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

package jnode.main.threads;

import java.util.TimerTask;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;

public class TimerPoll extends TimerTask {

	@Override
	public void run() {
		for (Link l : ORMManager.get(Link.class).getAll()) {
			if (FtnTools.getOptionBooleanDefTrue(l,
					LinkOption.BOOLEAN_POLL_BY_TIMEOT)) {
				PollQueue.getSelf().add(l);
			}
		}
	}
}
