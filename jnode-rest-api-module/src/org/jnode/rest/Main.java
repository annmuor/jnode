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

import jnode.dao.GenericDAO;
import jnode.dto.Link;
import jnode.dto.Robot;
import jnode.event.IEvent;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jnode.rest.core.CryptoUtils;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.ClassfileDependencyScanner;
import org.jnode.rest.di.Injector;
import org.jnode.rest.route.AuthFilter;
import org.jnode.rest.route.LoginServlet;
import org.jnode.rest.route.MainServlet;

import javax.servlet.DispatcherType;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.EnumSet;

public class Main extends JnodeModule {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final String REST_API_PORT = "rest-api-port";
    private static final String REST_API_SSL_PORT = "rest-api-ssl-port";
    private static final String REST_API_ADMIN = "rest-api-admin";
    private static final String REST_API_KEY_STORE_PATH = "rest-api-keyStorePath";
    private static final String REST_API_KEY_STORE_PASSWORD = "rest-api-keyStorePassword";
    private final int port;
    private final int sslPort;
    private final String keyStorePath;
    private final char[] keyStorePassword;
    private final String adminFtnAddress;

    public Main(String configFile) throws JnodeModuleException {
        super(configFile);
        port = getPort();
        sslPort = getSSLPort();
        keyStorePath = properties.getProperty(REST_API_KEY_STORE_PATH, "");
        keyStorePassword = properties.getProperty(REST_API_KEY_STORE_PASSWORD, "").toCharArray();

        adminFtnAddress = getAdminFtnAddressStr();
    }

    public static void main(String[] args) throws JnodeModuleException {
        Main mainModule = new Main(Main.class.getResource("config-rest.properties").getPath());
        mainModule.startForTest();
    }

    private int getPort() throws JnodeModuleException {
        try {
            return Integer.parseInt(properties.getProperty(REST_API_PORT, "4567"));
        } catch (NumberFormatException e) {
            throw new JnodeModuleException("bad port value", e);
        }
    }

    private int getSSLPort() throws JnodeModuleException {
        try {
            return Integer.parseInt(properties.getProperty(REST_API_SSL_PORT, "4568"));
        } catch (NumberFormatException e) {
            throw new JnodeModuleException("bad ssl port value", e);
        }
    }

    private String getAdminFtnAddressStr() throws JnodeModuleException {
        return properties.getProperty(REST_API_ADMIN, "");
    }

    @Override
    public void start() {
        try {
            startProd();
        } catch (Exception e) {
            LOGGER.l1("fail", e);
        }
    }

    private void startForTest() {
        try {
            startTest();
        } catch (Exception e) {
            LOGGER.l1("fail", e);
        }
    }

    private void startProd() throws JnodeModuleException {

        // даже если не будет использоваться дальше - надо вызвать, чтобы табличка создалась
        final GenericDAO<RestUser> dao = ORMManager.get(RestUser.class);

        if (!adminFtnAddress.isEmpty()) {
            Link adminLink = ORMManager.get(Link.class).getFirstAnd("linkAddress", "=", adminFtnAddress);
            if (adminLink != null) {
                RestUser admin = dao.getFirstAnd("link_id", "=", adminLink.getId());
                if (admin == null) {

                    final String pwd = CryptoUtils.randomToken();

                    admin = new RestUser();
                    admin.setLink(adminLink);
                    admin.setToken(CryptoUtils.sha256(pwd));
                    admin.setType(RestUser.Type.ADMIN);

                    dao.save(admin);

                    FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(), new FtnAddress(adminLink.getLinkAddress()),
                            UserRobot.REST_API_ROBOT, adminLink.getLinkName(), MessageFormat.format("{0} admin token",
                                    UserRobot.REST_API_ROBOT),
                            pwd);

                }
            }
        }

        Robot restApiRobot = new Robot();
        restApiRobot.setClassName(UserRobot.class.getCanonicalName());
        restApiRobot.setRobot("rest-api-robot");
        ORMManager.get(Robot.class).saveOrUpdate(restApiRobot);

        ClassfileDependencyScanner scanner = new ClassfileDependencyScanner();
        scanner.scan("org.jnode.rest", "prod-");

        try {
            Injector.inject(this);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new JnodeModuleException(e);
        }

        initJetty();
    }

    private void startTest() throws JnodeModuleException {
        ClassfileDependencyScanner scanner = new ClassfileDependencyScanner();
        scanner.scan("org.jnode.rest", "mock-");
        try {
            Injector.inject(this);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new JnodeModuleException(e);
        }

        initJetty();
    }

    private void initJetty() throws JnodeModuleException {

        boolean needSSL = keyStorePath != null && !keyStorePath.isEmpty();

        Server server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);

        if (needSSL){
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(keyStorePath);
            sslContextFactory.setKeyStorePassword(new String(keyStorePassword));
            sslContextFactory.setKeyManagerPassword(new String(keyStorePassword));
            ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
            sslConnector.setPort(sslPort);
            server.setConnectors(new Connector[]{connector, sslConnector});
        } else {
            server.setConnectors(new Connector[]{connector});

        }


        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(MainServlet.class, "/api");
        handler.addFilterWithMapping(AuthFilter.class, "/api", EnumSet.of(DispatcherType.REQUEST));
        handler.addServletWithMapping(LoginServlet.class, "/login");
        LOGGER.l5("ready");
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            throw new JnodeModuleException(e);
        }

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
