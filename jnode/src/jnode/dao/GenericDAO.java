package jnode.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import jnode.main.Main;

/**
 * Вот такое вот дао :)
 * 
 * @author kreon
 * 
 * @param <T>
 */
public abstract class GenericDAO<T> {
	private static HashMap<Class<?>, Dao<?, ?>> daoMap;
	private static ConnectionSource source;

	protected GenericDAO() throws Exception {
		if (source == null) {
			source = new JdbcConnectionSource(Main.getProperty(
					Main.Settings.JDBC_URL.getCfgline(), ""), Main.getProperty(
					Main.Settings.JDBC_USER.getCfgline(), ""),
					Main.getProperty(Main.Settings.JDBC_PASS.getCfgline(), ""));
		}
		if (daoMap == null) {
			daoMap = new HashMap<Class<?>, Dao<?, ?>>();
		}
		if (!daoMap.containsKey(getType())) {
			Dao<?, ?> dao = DaoManager.createDao(source, getType());
			if (!dao.isTableExists()) {
				TableUtils.createTable(source, getType());
			}
			daoMap.put(getType(), dao);
		}
	}

	abstract protected Class<?> getType();

	@SuppressWarnings("unchecked")
	private Dao<T, ?> getDao() {
		return (Dao<T, ?>) daoMap.get(getType());
	}

	@SuppressWarnings("unchecked")
	private <V> Dao<T, V> getDaoV() {
		return (Dao<T, V>) daoMap.get(getType());
	}

	public Where<T, ?> buildWhere(StatementBuilder<T, ?> sb, boolean and,
			Object... args) throws SQLException {
		if (args.length == 0) {
			return null;
		}
		Where<T, ?> wh = sb.where();
		boolean first = true;
		for (int i = 0; i < args.length; i += 3) {
			if (!first) {
				if (and)
					wh.and();
				else
					wh.or();
			} else {
				first = false;
			}
			String w = args[i + 1].toString();
			if ("eq".equals(w) || "=".equals(w) || "==".equals(w)) {
				wh.eq(args[i].toString(), args[i + 2]);
			} else if ("ne".equals(w) || "!=".equals(w) || "<>".equals(w)) {
				wh.ne(args[i].toString(), args[i + 2]);
			} else if ("gt".equals(w) || ">".equals(w)) {
				wh.gt(args[i].toString(), args[i + 2]);
			} else if ("ge".equals(w) || ">=".equals(w)) {
				wh.ge(args[i].toString(), args[i + 2]);
			} else if ("lt".equals(w) || "<".equals(w)) {
				wh.lt(args[i].toString(), args[i + 2]);
			} else if ("le".equals(w) || "<=".equals(w)) {
				wh.le(args[i].toString(), args[i + 2]);
			} else if ("like".equals(w) || "~".equals(w)) {
				wh.like(args[i].toString(), args[i + 2]);
			} else if ("in".equals(w)) {
				wh.in(args[i].toString(), (Iterable<?>) args[i + 2]);
			} else if ("between".equals(w)) {
				wh.between(args[i].toString(), args[i + 2], args[i + 3]);
				i += 1;
			}
		}
		return wh;
	}

	/**
	 * Получаем по ID
	 * 
	 * @param id
	 * @return
	 */
	public <V> T getById(V id) {
		try {
			return getDaoV().queryForId(id);
		} catch (SQLException e) {
		}
		return null;
	}

	/**
	 * Получить все
	 * 
	 * @return
	 */
	public List<T> getAll() {
		try {
			return getDao().queryForAll();
		} catch (SQLException e) {
		}
		return new ArrayList<T>();
	}

	/**
	 * Аргументы: a == b, c == d
	 * 
	 * @param args
	 * @return
	 */
	public List<T> getAnd(Object... args) {
		try {
			QueryBuilder<T, ?> qb = getDao().queryBuilder();
			buildWhere(qb, true, args);
			return qb.query();
		} catch (SQLException e) {
		}
		return new ArrayList<T>();
	}

	public List<T> getOrderAnd(String order, boolean asc, Object... args) {
		try {
			QueryBuilder<T, ?> qb = getDao().queryBuilder();
			qb.orderBy(order, asc);
			buildWhere(qb, true, args);
			return qb.query();
		} catch (SQLException e) {
		}
		return new ArrayList<T>();
	}

	public List<T> getOrderLimitAnd(long limit, String order, boolean asc,
			Object... args) {
		try {
			QueryBuilder<T, ?> qb = getDao().queryBuilder();
			qb.orderBy(order, asc);
			qb.limit(limit);
			buildWhere(qb, true, args);
			return qb.query();
		} catch (SQLException e) {
		}
		return new ArrayList<T>();
	}

	/**
	 * Аргументы: a == b, c == d
	 * 
	 * @param args
	 * @return
	 */
	public List<T> getOr(Object... args) {
		try {
			QueryBuilder<T, ?> qb = getDao().queryBuilder();
			buildWhere(qb, false, args);
			return qb.query();
		} catch (SQLException e) {
		}
		return new ArrayList<T>();
	}

	public List<T> getOrderOr(String order, boolean asc, Object... args) {
		try {
			QueryBuilder<T, ?> qb = getDao().queryBuilder();
			qb.orderBy(order, asc);
			buildWhere(qb, false, args);
			return qb.query();
		} catch (SQLException e) {
		}
		return new ArrayList<T>();
	}

	public T getFirstAnd(Object... args) {
		try {
			return getAnd(args).get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public T getFirstOr(Object... args) {
		try {
			return getOr(args).get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public GenericRawResults<String[]> getRaw(String query) {
		try {
			return getDao().queryRaw(query);
		} catch (SQLException e) {
		}
		return null;
	}

	public GenericRawResults<Object[]> getRaw(String query, DataType[] types) {
		try {
			return getDao().queryRaw(query, types);
		} catch (SQLException e) {
		}
		return null;
	}

	public void update(T object) {
		try {
			getDao().update(object);
		} catch (SQLException e) {
		}
	}

	public void save(T object) {
		try {
			getDao().create(object);
		} catch (SQLException e) {
		}
	}

	public void saveOrUpdate(T object) {
		try {
			getDao().createOrUpdate(object);
		} catch (SQLException e) {
		}
	}

	public void delete(T object) {
		try {
			getDao().delete(object);
		} catch (SQLException e) {
		}
	}

	public void update(String field, Object value, Object... args) {
		try {
			UpdateBuilder<T, ?> ub = getDao().updateBuilder();
			buildWhere(ub, true, args);
			ub.updateColumnValue(field, value);
			ub.update();
		} catch (SQLException e) {
		}
	}

	public void delete(Object... args) {
		try {
			DeleteBuilder<T, ?> db = getDao().deleteBuilder();
			buildWhere(db, true, args);
			db.delete();
		} catch (SQLException e) {
		}
	}

}
