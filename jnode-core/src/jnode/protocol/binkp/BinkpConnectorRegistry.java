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

package jnode.protocol.binkp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import jnode.protocol.binkp.connector.BinkpAbstractConnector;
import jnode.protocol.binkp.connector.BinkpAsyncConnector;
import jnode.protocol.binkp.connector.BinkpPipeConnector;
import jnode.protocol.binkp.connector.BinkpSyncConnector;

/**
 * Хранит в себе разные коннекторы - для модульности
 * 
 * @author kreon
 * 
 */
public class BinkpConnectorRegistry {
	private static final BinkpConnectorRegistry self = new BinkpConnectorRegistry();

	public static BinkpConnectorRegistry getSelf() {
		return self;
	}

	private HashMap<String, Class<? extends BinkpAbstractConnector>> connectorMap;

	private BinkpConnectorRegistry() {
		connectorMap = new HashMap<>();
		connectorMap.put("async:", BinkpAsyncConnector.class);
		connectorMap.put("sync:", BinkpSyncConnector.class);
		connectorMap.put("pipe:", BinkpPipeConnector.class);
		connectorMap.put("|", BinkpPipeConnector.class);
	}

	public synchronized void add(String key,
			Class<? extends BinkpAbstractConnector> clazz) {
		if (!connectorMap.containsKey(key)) {
			connectorMap.put(key, clazz);
		}
	}

	public Collection<String> getKeys() {
		return Collections.unmodifiableCollection(connectorMap.keySet());
	}

	public Class<? extends BinkpAbstractConnector> getConnector(String key) {
		return connectorMap.get(key);
	}
}
