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

public enum ORMManager {
	INSTANSE;
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
	private Dao<LinkOption, ?> daoLinkOption;

	/**
	 * Инициализация DAO и создание таблиц
	 * 
	 * @param settings
	 * @throws SQLException
	 */
	public void start(Hashtable<String, String> settings) throws SQLException {
		if (!started) {
			ConnectionSource source = new JdbcConnectionSource(
					settings.get(Main.Settings.JDBC_URL.getCfgline()),
					settings.get(Main.Settings.JDBC_USER.getCfgline()),
					settings.get(Main.Settings.JDBC_PASS.getCfgline()));
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
			daoLinkOption = DaoManager.createDao(source, LinkOption.class);

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
			if (!daoLinkOption.isTableExists()) {
				TableUtils.createTable(source, LinkOption.class);
			}
			started = true;
		}

	}

	public Dao<Link, Long> link() {
		return daoLink;
	}

	public Dao<Echoarea, Long> echoarea() {
		return daoEchoarea;
	}

	public Dao<Subscription, ?> subscription() {
		return daoSubscription;
	}

	public Dao<Echomail, Long> echomail() {
		return daoEchomail;
	}

	public Dao<Readsign, ?> readsign() {
		return daoReadsign;
	}

	public Dao<Netmail, Long> netmail() {
		return daoNetmail;
	}

	public Dao<Route, ?> route() {
		return daoRoute;
	}

	public Dao<Dupe, ?> dupe() {
		return daoDupe;
	}

	public Dao<Robot, String> robot() {
		return daoRobot;
	}

	public Dao<Rewrite, ?> rewrite() {
		return daoRewrite;
	}

	public Dao<LinkOption, ?> linkoption() {
		return daoLinkOption;
	}
}
