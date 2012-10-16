package jnode.orm;

import jnode.dao.GenericDAO;
import jnode.dto.*;

/**
 * Singleton
 * 
 * @author kreon
 * 
 */

public enum ORMManager {
	INSTANSE;
	private GenericDAO<Dupe> dupeDAO;
	private GenericDAO<Echoarea> echoareaDAO;
	private GenericDAO<Echomail> echomailDAO;
	private GenericDAO<Link> linkDAO;
	private GenericDAO<LinkOption> linkOptionDAO;
	private GenericDAO<Netmail> netmailDAO;
	private GenericDAO<Rewrite> rewriteDAO;
	private GenericDAO<Robot> robotDAO;
	private GenericDAO<Route> routeDAO;
	private GenericDAO<Subscription> subscriptionDAO;
	private GenericDAO<EchomailAwaiting> echomailAwaitingDAO;

	public void start() throws Exception {
		dupeDAO = new GenericDAO<Dupe>() {

			@Override
			protected Class<?> getType() {
				return Dupe.class;
			}
		};
		echoareaDAO = new GenericDAO<Echoarea>() {

			@Override
			protected Class<?> getType() {
				return Echoarea.class;
			}
		};
		echomailDAO = new GenericDAO<Echomail>() {

			@Override
			protected Class<?> getType() {
				return Echomail.class;
			}
		};
		linkDAO = new GenericDAO<Link>() {

			@Override
			protected Class<?> getType() {
				return Link.class;
			}
		};
		linkOptionDAO = new GenericDAO<LinkOption>() {

			@Override
			protected Class<?> getType() {
				return LinkOption.class;
			}
		};
		netmailDAO = new GenericDAO<Netmail>() {

			@Override
			protected Class<?> getType() {
				return Netmail.class;
			}
		};
		rewriteDAO = new GenericDAO<Rewrite>() {

			@Override
			protected Class<?> getType() {
				return Rewrite.class;
			}
		};
		robotDAO = new GenericDAO<Robot>() {

			@Override
			protected Class<?> getType() {
				return Robot.class;
			}
		};
		routeDAO = new GenericDAO<Route>() {

			@Override
			protected Class<?> getType() {
				return Route.class;
			}
		};
		subscriptionDAO = new GenericDAO<Subscription>() {

			@Override
			protected Class<?> getType() {
				return Subscription.class;
			}
		};
		echomailAwaitingDAO = new GenericDAO<EchomailAwaiting>() {

			@Override
			protected Class<?> getType() {
				return EchomailAwaiting.class;
			}
		};
	}

	public GenericDAO<Dupe> getDupeDAO() {
		return dupeDAO;
	}

	public GenericDAO<Echoarea> getEchoareaDAO() {
		return echoareaDAO;
	}

	public GenericDAO<Echomail> getEchomailDAO() {
		return echomailDAO;
	}

	public GenericDAO<Link> getLinkDAO() {
		return linkDAO;
	}

	public GenericDAO<LinkOption> getLinkOptionDAO() {
		return linkOptionDAO;
	}

	public GenericDAO<Netmail> getNetmailDAO() {
		return netmailDAO;
	}


	public GenericDAO<Rewrite> getRewriteDAO() {
		return rewriteDAO;
	}

	public GenericDAO<Robot> getRobotDAO() {
		return robotDAO;
	}

	public GenericDAO<Route> getRouteDAO() {
		return routeDAO;
	}

	public GenericDAO<Subscription> getSubscriptionDAO() {
		return subscriptionDAO;
	}

	public GenericDAO<EchomailAwaiting> getEchomailAwaitingDAO() {
		return echomailAwaitingDAO;
	}

}
