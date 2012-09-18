package jnode.orm;

import java.sql.SQLException;
import java.util.Hashtable;

import jnode.dto.*;
import jnode.main.Main;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Singleton
 * 
 * @author kreon
 * 
 */

public class ORMManager {
	private static ORMManager self = new ORMManager();
	private boolean started = false;
	private Dao<Link, Long> daoLink;
	private Dao<Echoarea, Long> daoEchoarea;
	private Dao<Subscription, ?> daoSubscription;
	private Dao<Echomail, Long> daoEchomail;
	private Dao<Readsign, ?> daoReadsign;
	private Dao<Netmail, Long> daoNetmail;
	private Dao<Route, ?> daoRoute;
	private Dao<Dupe, ?> daoDupe;
	private Dao<Robot, String> daoRobot;
	private Dao<Rewrite, ?> daoRewrite;

	public static ORMManager getInstanse() {
		return self;
	}

	/**
	 * Инициализация DAO и создание таблиц
	 * 
	 * @param settings
	 * @throws SQLException
	 */
	public void start(Hashtable<String, String> settings) throws SQLException {
		if (!started) {
			ConnectionSource source = new JdbcConnectionSource(settings.get(Main.Settings.JDBC_URL.getCfgline()),
					settings.get(Main.Settings.JDBC_USER.getCfgline()), settings.get(Main.Settings.JDBC_PASS
							.getCfgline()));
			daoLink = DaoManager.createDao(source, Link.class);
			daoEchoarea = DaoManager.createDao(source, Echoarea.class);
			daoSubscription = DaoManager.createDao(source, Subscription.class);
			daoEchomail = DaoManager.createDao(source, Echomail.class);
			daoReadsign = DaoManager.createDao(source, Readsign.class);
			daoNetmail = DaoManager.createDao(source, Netmail.class);
			daoRoute = DaoManager.createDao(source, Route.class);
			daoDupe = DaoManager.createDao(source, Dupe.class);
			daoRobot = DaoManager.createDao(source, Robot.class);
			daoRewrite = DaoManager.createDao(source, Rewrite.class);

			if (!daoLink.isTableExists()) {
				TableUtils.createTable(source, Link.class);
			}
			if (!daoEchoarea.isTableExists()) {
				TableUtils.createTable(source, Echoarea.class);
			}
			if (!daoSubscription.isTableExists()) {
				TableUtils.createTable(source, Subscription.class);
			}
			if (!daoEchomail.isTableExists()) {
				TableUtils.createTable(source, Echomail.class);
			}
			if (!daoReadsign.isTableExists()) {
				TableUtils.createTable(source, Readsign.class);
			}
			if (!daoNetmail.isTableExists()) {
				TableUtils.createTable(source, Netmail.class);
			}
			if (!daoRoute.isTableExists()) {
				TableUtils.createTable(source, Route.class);
			}
			if (!daoDupe.isTableExists()) {
				TableUtils.createTable(source, Dupe.class);
			}
			if (!daoRobot.isTableExists()) {
				TableUtils.createTable(source, Robot.class);
			}
			if (!daoRewrite.isTableExists()) {
				TableUtils.createTable(source, Rewrite.class);
			}
			started = true;
		}

	}

	public static Dao<Link, Long> link() {
		return getInstanse().daoLink;
	}

	public static Dao<Echoarea, Long> echoarea() {
		return getInstanse().daoEchoarea;
	}

	public static Dao<Subscription, ?> subscription() {
		return getInstanse().daoSubscription;
	}

	public static Dao<Echomail, Long> echomail() {
		return getInstanse().daoEchomail;
	}

	public static Dao<Readsign, ?> readsign() {
		return getInstanse().daoReadsign;
	}

	public static Dao<Netmail, Long> netmail() {
		return getInstanse().daoNetmail;
	}

	public static Dao<Route, ?> route() {
		return getInstanse().daoRoute;
	}

	public static Dao<Dupe, ?> dupe() {
		return getInstanse().daoDupe;
	}

	public static Dao<Robot, String> robot() {
		return getInstanse().daoRobot;
	}

	public static Dao<Rewrite, ?> rewrite() {
		return getInstanse().daoRewrite;
	}
}
