package jnode.orm;

import java.util.HashMap;
import java.util.Map;

import jnode.dao.GenericDAO;
import jnode.dto.*;
import jnode.logger.Logger;

/**
 * Singleton
 * 
 * @author kreon
 * 
 */

public enum ORMManager {
	INSTANSE;
	private static final Logger logger = Logger.getLogger(ORMManager.class);
	private Map<Class<?>, GenericDAO<?>> genericDAOMap = new HashMap<Class<?>, GenericDAO<?>>();

	public void start() throws Exception {

	}

	@Deprecated
	public GenericDAO<Dupe> getDupeDAO() {
		return get(Dupe.class);
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
				logger.l1("jNode have to shutdow during critical error");
				System.exit(0);
			}
		}
		return ret;
	}
}
