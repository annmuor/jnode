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

import java.io.IOException;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.PollQueue;
import jnode.main.threads.ThreadPool;
import jnode.protocol.binkp.connector.BinkpAbstractConnector;
import jnode.protocol.binkp.connector.BinkpAsyncConnector;

public class BinkpAsyncClientPool implements Runnable {
	private static final Logger logger = Logger
			.getLogger(BinkpAsyncClientPool.class);
	private static final String BINKD_CLIENT = "binkp.client";

	@Override
	public void run() {
		if (!MainHandler.getCurrentInstance().getBooleanProperty(BINKD_CLIENT,
				true)) {
			return;
		}
		while (true) {
			Link l = null;
			synchronized (PollQueue.getSelf()) {
				if (PollQueue.getSelf().isEmpty()) {
					try {
						PollQueue.getSelf().wait();
					} catch (InterruptedException e) {
					}
				}
				l = PollQueue.getSelf().getNext();
			}
			try {
				BinkpAbstractConnector conn = null;
				String pa = l.getProtocolAddress();
				for (String key : BinkpConnectorRegistry.getSelf().getKeys()) {
					if (l.getProtocolAddress().startsWith(key)) {
						conn = createConnector(pa, key);
						break;
					}
				}
				if (conn == null) {
					conn = new BinkpAsyncConnector(l.getProtocolAddress());
				}
				ThreadPool.execute(conn);
			} catch (RuntimeException e) {
				logger.l2("Runtime exception: " + e.getLocalizedMessage(), e);
			} catch (IOException e) {
				logger.l2(e.getLocalizedMessage(), e);
			}
		}
	}

	protected BinkpAbstractConnector createConnector(String protocolAddress,
			String key) throws IOException {
		Class<? extends BinkpAbstractConnector> connectorClass = BinkpConnectorRegistry
				.getSelf().getConnector(key);
		try {
			return connectorClass.getConstructor(String.class).newInstance(
					protocolAddress.substring(key.length()));
		} catch (Exception e) {
			throw new IOException("Error instatiating class "
					+ connectorClass.getName() + " ( " + protocolAddress
					+ " ) ", e);
		}
	}
}
