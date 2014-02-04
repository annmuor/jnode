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

package jnode.orm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import jnode.dao.GenericDAO;
import jnode.dto.*;
import jnode.install.support.Dupe_1_4;
import jnode.logger.Logger;
import jnode.main.MainHandler;

/**
 * Singleton
 * 
 * @author kreon
 * 
 */

public enum ORMManager {
	INSTANSE;

	public final static String JDBC_URL = "jdbc.url";
	public final static String JDBC_USER = "jdbc.user";
	public final static String JDBC_PASS = "jdbc.pass";

	private static final Logger logger = Logger.getLogger(ORMManager.class);
	private Map<Class<?>, GenericDAO<?>> genericDAOMap = new HashMap<>();
	private ConnectionSource source;

	public void start() throws Exception {
		try {
		source = new JdbcConnectionSource(MainHandler.getCurrentInstance()
				.getProperty(JDBC_URL, ""), MainHandler.getCurrentInstance()
				.getProperty(JDBC_USER, ""), MainHandler.getCurrentInstance()
				.getProperty(JDBC_PASS, ""));
		} catch(SQLException e) {
			throw new Exception("Exception in source creation", e);
		}
	}

	@Deprecated
	public GenericDAO<Dupe_1_4> getDupeDAO() {
		return get(Dupe_1_4.class);
	}

	@Deprecated
	public GenericDAO<Echoarea> getEchoareaDAO() {
		return get(Echoarea.class);
	}

	@Deprecated
	public GenericDAO<Echomail> getEchomailDAO() {
		return get(Echomail.class);
	}

	@Deprecated
	public GenericDAO<EchomailAwaiting> getEchomailAwaitingDAO() {
		return get(EchomailAwaiting.class);
	}

	@Deprecated
	public GenericDAO<Filearea> getFileareaDAO() {
		return get(Filearea.class);
	}

	@Deprecated
	public GenericDAO<Filemail> getFilemailDAO() {
		return get(Filemail.class);
	}

	@Deprecated
	public GenericDAO<FilemailAwaiting> getFilemailAwaitingDAO() {
		return get(FilemailAwaiting.class);
	}

	@Deprecated
	public GenericDAO<FileSubscription> getFileSubscriptionDAO() {
		return get(FileSubscription.class);
	}

	@Deprecated
	public GenericDAO<Link> getLinkDAO() {
		return get(Link.class);
	}

	@Deprecated
	public GenericDAO<LinkOption> getLinkOptionDAO() {
		return get(LinkOption.class);
	}

	@Deprecated
	public GenericDAO<Netmail> getNetmailDAO() {
		return get(Netmail.class);
	}

	@Deprecated
	public GenericDAO<Rewrite> getRewriteDAO() {
		return get(Rewrite.class);
	}

	@Deprecated
	public GenericDAO<Robot> getRobotDAO() {
		return get(Robot.class);
	}

	@Deprecated
	public GenericDAO<Route> getRouteDAO() {
		return get(Route.class);
	}

	@Deprecated
	public GenericDAO<Subscription> getSubscriptionDAO() {
		return get(Subscription.class);
	}

	@Deprecated
	public GenericDAO<Jscript> getJscriptDAO() {
		return get(Jscript.class);
	}

	@Deprecated
	public GenericDAO<Schedule> getScheduleDAO() {
		return get(Schedule.class);
	}

	@Deprecated
	public GenericDAO<ScriptHelper> getScriptHelperDAO() {
		return get(ScriptHelper.class);
	}

	@Deprecated
	public GenericDAO<FileForLink> getFileForLinkDAO() {
		return get(FileForLink.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> GenericDAO<T> get(final Class<? extends T> clazz) {
		GenericDAO<T> ret = (GenericDAO<T>) INSTANSE.genericDAOMap.get(clazz);
		if (ret == null) {
			try {
				ret = new GenericDAO<T>() {

					@Override
					protected Class<?> getType() {
						// TODO Auto-generated method stub
						return clazz;
					}
				};
				INSTANSE.genericDAOMap.put(clazz, ret);
			} catch (Exception e) {
				for (int i = 0; i < 10; i++)
					logger.l1("!!! FATAL !!! Exception while creation DAO for "
							+ clazz.getCanonicalName());
				logger.l1("jNode have to shutdow during critical error", e);
				System.exit(0);
			}
		}
		return ret;
	}

	public static ConnectionSource getSource() throws Exception {
		if (INSTANSE.source != null) {
			return INSTANSE.source;
		}
		synchronized (INSTANSE) {
			INSTANSE.start();
			return INSTANSE.source;
		}
	}
}
