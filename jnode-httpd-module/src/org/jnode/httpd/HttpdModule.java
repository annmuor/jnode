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

package org.jnode.httpd;

import jnode.event.IEvent;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;
import org.jnode.httpd.filters.*;
import org.jnode.httpd.routes.get.*;
import org.jnode.httpd.routes.post.*;
import org.jnode.httpd.util.HTML;

/**
 * HttpdModule - модуль, слушающий порт и отдающий странички
 * 
 * @author kreon
 * 
 */
import spark.Spark;

/**
 * 
 * @author kreon
 * 
 */
public class HttpdModule extends JnodeModule {
	private static final String CONFIG_PORT = "port";
	private static final String CONFIG_LINK_REG = "linkreg";
	private static final String CONFIG_POINT_REG = "pointreg";
	private static final String CONFIG_EXTERNAL = "external";

	private static final Logger logger = Logger.getLogger(HttpdModule.class);
	private short port;
	private boolean linkreg;
	private boolean pointreg;
	private String external;

	public HttpdModule(String configFile) throws JnodeModuleException {
		super(configFile);
		port = Short.valueOf(properties.getProperty(CONFIG_PORT, "8080"));
		linkreg = Boolean.valueOf(properties.getProperty(CONFIG_LINK_REG,
				"false"));
		pointreg = Boolean.valueOf(properties.getProperty(CONFIG_POINT_REG,
				"false"));
		external = properties.getProperty(CONFIG_EXTERNAL);
		HTML.setExternalPath(external);
	}

	@Override
	public void handle(IEvent event) {

	}

	@Override
	public void start() {

		Spark.setPort(port);
		if (external != null) {
			Spark.externalStaticFileLocation(external);
		}
		Spark.staticFileLocation("/www");

		/**** PUBLIC LINKS *****/
		Spark.get(new SelfRoute());
		Spark.get(new SelfRoute("/"));
		Spark.get(new SelfRoute(""));
		if (pointreg) {
			Spark.get(new BecomePointRoute(true));
			Spark.get(new PointRequestConfirmRoute());
			Spark.post(new PointRequestRoute());
		} else {
			Spark.get(new BecomePointRoute(false));
		}
		if (linkreg) {
			Spark.get(new BecomeLinkRoute(true));
			Spark.post(new LinkRequestRoute());
		} else {
			Spark.get(new BecomeLinkRoute(false));
		}

		Spark.before(new SecureFilter("/secure/*"));
		Spark.after(new CharsetFilter());

		/**** SECURE LINKS ****/
		Spark.get(new HealthRoute());
		Spark.get(new LinksRoute());
		Spark.get(new LinkoptionsRoute());
		Spark.get(new EchoareasRoute());
		Spark.get(new FileareasRoute());
		Spark.get(new RoutingsRoute());
		Spark.get(new RewritesRoute());
		Spark.get(new UsersRoute());

		Spark.post(new LinkRoute());
		Spark.post(new LinkoptionRoute());
		Spark.post(new EchoareaRoute());
		Spark.post(new FileareaRoute());
		Spark.post(new RoutingRoute());
		Spark.post(new RewriteRoute());
		Spark.post(new UserRoute());

		try {
			WebAdmin admin = ORMManager.get(WebAdmin.class).getFirstAnd();
			if (admin == null) {
				admin = new WebAdmin();
				admin.setUsername("admin");
				String password = FtnTools.generate8d();
				admin.setPassword(FtnTools.md5(password));
				ORMManager.get(WebAdmin.class).save(admin);
				String text = "You can login to jNode site with those credentials:\n  > login: admin\n > password "
						+ password + "\n";
				// write netmail to primary address
				FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
						FtnTools.getPrimaryFtnAddress(), "HTTPD Module",
						"Sysop of Node", "Web password", text);
				logger.l1("Admin created\n" + text);
			}
		} catch (RuntimeException e) {
		}
	}
}
