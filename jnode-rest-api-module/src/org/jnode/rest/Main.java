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

package org.jnode.rest;

import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;
import org.jnode.rest.auth.BasicAuthenticationFilter;
import org.jnode.rest.auth.PwdProvider;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.route.PostEchoareaRoute;
import org.jnode.rest.route.PostNetmailRoute;
import spark.Spark;

public class Main extends JnodeModule {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private final int port;

    public Main(String configFile) throws JnodeModuleException {
        super(configFile);
        port = getPort();
        ORMManager.get(RestUser.class);
    }

    private int getPort() throws JnodeModuleException {
        try {
            return Integer.parseInt(properties.getProperty("rest-api-port", "4567"));
        } catch (NumberFormatException e) {
            throw new JnodeModuleException("bad port value", e);
        }
    }

    @Override
    public void start() {
        try {
            startInternal();
        } catch (Exception e) {
            LOGGER.l1("fail", e);
        }
    }

    private void startInternal() throws JnodeModuleException {
        Spark.setPort(port);
        Spark.before(new BasicAuthenticationFilter(new PwdProvider()));
        Spark.post(new PostEchoareaRoute("/echoarea"));
        Spark.post(new PostNetmailRoute("/netmail"));
    }

    @Override
    public void handle(IEvent iEvent) {

    }

    @Override
    public String toString() {
        return "Main{" +
                "port=" + port +
                "} " + super.toString();
    }
}
