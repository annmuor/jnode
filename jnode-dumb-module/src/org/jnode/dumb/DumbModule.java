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

package org.jnode.dumb;

import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;

public class DumbModule extends JnodeModule {
	public DumbModule(String configFile) throws JnodeModuleException {
		super(configFile);
	}

	private static final Logger logger = Logger.getLogger(DumbModule.class);

	@Override
	public void handle(IEvent event) {

	}

	@Override
	public void start() {
		long delay = new Long(properties.getProperty("delay"));
		synchronized (this) {
			while (true) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
				logger.l1("Dumb module still alive");
			}
		}
	}

}
