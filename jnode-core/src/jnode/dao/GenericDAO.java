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

package jnode.dao;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jnode.logger.Logger;
import jnode.orm.ORMManager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;

/**
 * Вот такое вот дао :)
 * 
 * @author kreon
 * 
 * @param <T>
 */
public abstract class GenericDAO<T> {

	private static HashMap<Class<?>, Dao<?, ?>> daoMap;

	private final Logger logger = Logger.getLogger(getType());

	protected GenericDAO() throws Exception {
		if (daoMap == null) {
			daoMap = new HashMap<>();
		}
		if (!daoMap.containsKey(getType())) {
			Dao<?, ?> dao = DaoManager.createDao(ORMManager.getSource(),
					getType());
			if (!dao.isTableExists()) {
				TableUtils.createTable(ORMManager.getSource(), getType());
			}
			daoMap.put(getType(), dao);
		}
	}

	abstract protected Class<?> getType();

	@SuppressWarnings("unchecked")
	Dao<T, ?> getDao() {
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
			switch (w) {
			case "eq":
			case "=":
			case "==":
				wh.eq(args[i].toString(), new SelectArg(args[i + 2]));
				break;
			case "null":
				wh.isNull(args[i].toString());
				i -= 1;
				break;
			case "notnull":
				wh.isNotNull(args[i].toString());
				i -= 1;
				break;
			case "ne":
			case "!=":
			case "<>":
				wh.ne(args[i].toString(),  new SelectArg(args[i + 2]));
				break;
			case "gt":
			case ">":
				wh.gt(args[i].toString(),  new SelectArg(args[i + 2]));
				break;
			case "ge":
			case ">=":
				wh.ge(args[i].toString(),  new SelectArg(args[i + 2]));
				break;
			case "lt":
			case "<":
				wh.lt(args[i].toString(),  new SelectArg(args[i + 2]));
				break;
			case "le":
			case "<=":
				wh.le(args[i].toString(),  new SelectArg(args[i + 2]));
				break;
			case "like":
			case "~":
				wh.like(args[i].toString(),  new SelectArg(args[i + 2]));
				break;
			case "in":
				wh.in(args[i].toString(),  (Iterable<?>) args[i + 2]);
				break;
			case "between":
				wh.between(args[i].toString(),  new SelectArg(args[i + 2]),  new SelectArg(args[i + 3]));
				i += 1;
				break;
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
			logger.l1("SQL Exception in getById", e);
			logger.l1(MessageFormat.format("we worked with {0}", e));
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
			logger.l1("SQL Exception in getAll", e);
			logger.l1(MessageFormat.format("we worked with {0}", e));
		}
		return new ArrayList<>();
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
			logger.l1("SQL Exception in getAnd", e);
			logger.l1(MessageFormat.format("we worked with {0}",
					Arrays.toString(args)));

		}
		return new ArrayList<>();
	}

	public List<T> getOrderAnd(String order, boolean asc, Object... args) {
		try {
			QueryBuilder<T, ?> qb = getDao().queryBuilder();
			qb.orderBy(order, asc);
			buildWhere(qb, true, args);
			return qb.query();
		} catch (SQLException e) {
			logger.l1("SQL Exception in getOrderAnd", e);
			logger.l1(MessageFormat.format("we worked with {0} {1} {2}", order,
					asc, Arrays.toString(args)));
		}
		return new ArrayList<>();
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
			logger.l1("SQL Exception in getOrderLimitAnd", e);
			logger.l1(MessageFormat.format("we worked with {0} {1} {2} {3}",
					limit, order, asc, Arrays.toString(args)));
		}
		return new ArrayList<>();
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
			logger.l1("SQL Exception in getOr", e);
			logger.l1(MessageFormat.format("we worked with {0}",
					Arrays.toString(args)));
		}
		return new ArrayList<>();
	}

	public List<T> getOrderOr(String order, boolean asc, Object... args) {
		try {
			QueryBuilder<T, ?> qb = getDao().queryBuilder();
			qb.orderBy(order, asc);
			buildWhere(qb, false, args);
			return qb.query();
		} catch (SQLException e) {
			logger.l1("SQL Exception in getOrderOr", e);
			logger.l1(MessageFormat.format("we worked with {0} {1} {2}", order,
					asc, Arrays.toString(args)));
		}
		return new ArrayList<>();
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
			logger.l1("SQL Exception in getRaw", e);
			logger.l1(MessageFormat.format("we worked with {0}", query));
		}
		return null;
	}

	public GenericRawResults<Object[]> getRaw(String query, DataType[] types) {
		try {
			return getDao().queryRaw(query, types);
		} catch (SQLException e) {
			logger.l1("SQL Exception in getRaw", e);
			logger.l1(MessageFormat.format("we worked with {0} {1}", query,
					Arrays.toString(types)));
		}
		return null;
	}

	public void update(T object) {
		try {
			getDao().update(object);
		} catch (SQLException e) {
			logger.l1("SQL Exception in update", e);
			logger.l1(MessageFormat.format("we worked with {0}", object));
		}
	}

	public void save(T object) {
		try {
			getDao().create(object);
		} catch (SQLException e) {
			logger.l1("SQL Exception in save", e);
			logger.l1(MessageFormat.format("we worked with {0}", object));
		}
	}

	public void saveOrUpdate(T object) {
		try {
			getDao().createOrUpdate(object);
		} catch (SQLException e) {
			logger.l1("SQL Exception in saveOrUpdate", e);
			logger.l1(MessageFormat.format("we worked with {0}", object));
		}
	}

	public void delete(T object) {
		try {
			getDao().delete(object);
		} catch (SQLException e) {
			logger.l1("SQL Exception in delete", e);
			logger.l1(MessageFormat.format("we worked with {0}", object));
		}
	}

	public void update(String field, Object value, Object... args) {
		try {
			UpdateBuilder<T, ?> ub = getDao().updateBuilder();
			buildWhere(ub, true, args);
			ub.updateColumnValue(field, value);
			ub.update();
		} catch (SQLException e) {
			logger.l1("SQL Exception in update", e);
			logger.l1(MessageFormat.format("we worked with {0} {1} {2}", field,
					value, Arrays.toString(args)));
		}
	}

	public void delete(Object... args) {
		try {
			DeleteBuilder<T, ?> db = getDao().deleteBuilder();
			buildWhere(db, true, args);
			db.delete();
		} catch (SQLException e) {
			logger.l1("SQL Exception in delete", e);
			logger.l1(MessageFormat.format("we worked with {0}",
					Arrays.toString(args)));
		}
	}

	public void executeRaw(String query) {
		try {
			getDao().executeRawNoArgs(query);
		} catch (SQLException e) {
			logger.l1("SQL Exception in executeRaw", e);
			logger.l1(MessageFormat.format("we worked with {0}", query));
		}
	}

	public QueryJoiner join(boolean and, Object... args) {
		try {
			return new QueryJoiner(and, args);
		} catch (SQLException e) {
			logger.l2("Error creating QueryJoiner");
		}
		return null;
	}

	public class QueryJoiner {
		private QueryBuilder<T, ?> qb;

		public <Z> QueryJoiner join(Class<Z> clazz, boolean and, Object... args) {
			try {
				QueryBuilder<Z, ?> nqb = ORMManager.get(clazz).getDao()
						.queryBuilder();
				ORMManager.get(clazz).buildWhere(nqb, and, args);
				qb.join(nqb);
			} catch (SQLException e) {
				logger.l2("SQL error while joining", e);
			}
			return this;
		}

		public QueryJoiner(boolean and, Object... args) throws SQLException {
			qb = (QueryBuilder<T, ?>) getDao().queryBuilder();
			buildWhere(qb, and, args);
		}

		public List<T> list() {
			try {
				return qb.query();
			} catch (SQLException e) {
				logger.l2("SQL error while query", e);
			}
			return new ArrayList<>();
		}

		public T one() {
			try {
				return qb.queryForFirst();
			} catch (SQLException e) {
				logger.l2("SQL error while query one", e);
			}
			return null;
		}
	}

}
