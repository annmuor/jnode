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
