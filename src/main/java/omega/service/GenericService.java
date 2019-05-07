package omega.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.core.BeanUtility;
import omega.core.Core;
import omega.persistence.PersistenceService;

public class GenericService {

	@Inject
	PersistenceService persistenceService;

	public static void prepare(PreparedStatement stmt, Object... array) throws SQLException {
		int i = 1;
		for (Object object : array) {
			if (object == null) {
				stmt.setNull(i++, java.sql.Types.NULL);
			} else if (object instanceof String) {
				stmt.setString(i++, (String) object);
			} else if (object instanceof Long) {
				stmt.setLong(i++, (Long) object);
			} else if (object instanceof Integer) {
				stmt.setInt(i++, (Integer) object);
			} else if (object instanceof Boolean) {
				stmt.setBoolean(i++, (Boolean) object);
			} else if (object instanceof Enum) {
				stmt.setString(i++, ((Enum) object).name());
			} else if (object instanceof java.math.BigDecimal) {
				stmt.setObject(i++, (java.math.BigDecimal) object);
			} else if (object instanceof java.sql.Timestamp) {
				stmt.setTimestamp(i++, (java.sql.Timestamp) object);
			} else if (object instanceof java.sql.Date) {
				stmt.setDate(i++, (java.sql.Date) object);
			} else if (object instanceof java.time.LocalDate) {
				stmt.setObject(i++, object);
			} else if (object instanceof java.time.LocalDateTime) {
				stmt.setObject(i++, object);
			} else if (object instanceof java.util.Date) {
				stmt.setDate(i++, convertDate((java.util.Date) object));
			}
		}
	}

	public static <T> String join(T[] array, String delimiter, String open, String close) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (T t : array) {
			if (!first) {
				sb.append(delimiter);
			} else {
				first = false;
			}
			if (open != null) {
				sb.append(open);
			}
			sb.append(t);
			if (close != null) {
				sb.append(close);
			}
		}
		return sb.toString();
	}

	public static <T> String join(T[] array, String delimiter) {
		return join(array, delimiter, null, null);
	}

	public static <T> String join(List<T> list, String delimiter, String open, String close) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (T t : list) {
			if (!first) {
				sb.append(delimiter);
			} else {
				first = false;
			}
			if (open != null) {
				sb.append(open);
			}
			sb.append(t);
			if (close != null) {
				sb.append(close);
			}
		}
		return sb.toString();
	}

	public static <T> String join(List<T> list, String delimiter) {
		return join(list, delimiter, null, null);
	}

	public static String repeat(String element, int multiplier, String delimiter) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < multiplier; i++) {
			list.add(element);
		}
		return join(list, ",");
	}

	public static java.sql.Date convertDate(java.util.Date date) {
		if (date != null) {
			return new java.sql.Date(date.getTime());
		}
		return null;
	}

	// low level

	@Transactional(type = TransactionType.READWRITE)
	public int write(String sql, Object... array) {
		int result = 0;
		Connection connection = persistenceService.get().getConnection();
		// System.out.println("connection = " + connection);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			prepare(stmt, array);
			result = stmt.executeUpdate();
			stmt.close();
			stmt = null;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	@Transactional(type = TransactionType.READONLY)
	public <T> List<T> read(boolean asList, Builder<T> builder, String sql, Object... array) {
		List<T> resultList = new ArrayList<>();
		Connection connection = persistenceService.get().getConnection();
		// System.out.println("connection = " + connection);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			prepare(stmt, array);
			rs = stmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					T entity = builder.build(rs);
					resultList.add(entity);
					if (!asList) {
						break;
					}
				}
				rs.close();
				rs = null;
			}
			stmt.close();
			stmt = null;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return resultList;
	}

	// medium level

	@Transactional(type = TransactionType.READWRITE)
	public <T> int insert(String tableName, T entity, String... array) {
		String sql = "insert into " + tableName + " (" + join(array, ",") + ") values (" + repeat("?", array.length, ",") + ")";
		List parameterList = new ArrayList<>();
		try {
			for (String entry : array) {
				parameterList.add(BeanUtility.instance().getPropertyUtils().getProperty(entity, entry));
			}
			return write(sql, parameterList.toArray());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Transactional(type = TransactionType.READWRITE)
	public <T> int update(String tableName, T entity, String[] qualifierArray, String... array) {
		StringBuilder sqlQualifier = new StringBuilder();
		boolean first = true;
		for (String qualifier : qualifierArray) {
			if (first) {
				first = false;
			} else {
				sqlQualifier.append(" and ");
			}
			sqlQualifier.append(qualifier + " = ?");
		}
		List<String> setList = new ArrayList<>();
		for (String element : array) {
			setList.add(element + " = ?");
		}
		String sql = "update " + tableName + " set " + join(setList, ",") + " where (" + sqlQualifier.toString() + ")";
		List parameterList = new ArrayList<>();
		try {
			for (String entry : array) {
				parameterList.add(BeanUtility.instance().getPropertyUtils().getProperty(entity, entry));
			}
			for (String qualifier : qualifierArray) {
				parameterList.add(BeanUtility.instance().getPropertyUtils().getProperty(entity, qualifier));
			}
			return write(sql, parameterList.toArray());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Transactional(type = TransactionType.READONLY)
	public <T> List<T> read(boolean asList, Class<T> clazz, Map<Class, Class> classMap, Map<String, Class> columnTypeMap, String sql, Object... array) {
		List<T> resultList = new ArrayList<>();

		Map<String, String> fieldMap = new HashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			fieldMap.put(field.getName().toLowerCase(), field.getName());
		}

		Connection connection = persistenceService.get().getConnection();
		// System.out.println("connection = " + connection);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			prepare(stmt, array);
			rs = stmt.executeQuery();
			if (rs != null) {
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();
				while (rs.next()) {
					T entity = Core.getInjector().getInstance(clazz);
					try {
						for (int column = 1; column <= columnCount; column++) {
							String databaseFieldName = metaData.getColumnName(column).toLowerCase();
							String entityFieldName = fieldMap.get(databaseFieldName);
							if (entityFieldName != null) {
								if (columnTypeMap != null && !columnTypeMap.isEmpty() && columnTypeMap.containsKey(entityFieldName)) {
									BeanUtility.instance().setProperty(entity, entityFieldName, rs.getObject(column, columnTypeMap.get(entityFieldName)));
								} else {
									Object value = rs.getObject(column);
									if (classMap != null && !classMap.isEmpty() && value != null) {
										if (classMap.containsKey(value.getClass())) {
											value = rs.getObject(column, classMap.get(value.getClass()));
										}
									}
									BeanUtility.instance().setProperty(entity, entityFieldName, value);
								}
							}
						}
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					resultList.add(entity);
					if (!asList) {
						break;
					}
				}
				rs.close();
				rs = null;
			}
			stmt.close();
			stmt = null;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return resultList;
	}

	// high level

	public <T> List<T> list(Builder<T> builder, String sql, Object... array) {
		return read(true, builder, sql, array);
	}

	public <T> T find(Builder<T> builder, String sql, Object... array) {
		List<T> list = read(false, builder, sql, array);
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public <T> T select(Class<T> clazz, String sql, Object... array) {
		Builder<T> builder = new Builder<T>() {
			public T build(ResultSet rs) throws SQLException {
				return rs.getObject(1, clazz);
			}
		};

		return find(builder, sql, array);
	}

	public <T> List<T> list(Class<T> clazz, Map<Class, Class> classMap, Map<String, Class> columnTypeMap, String sql, Object... array) {
		return read(true, clazz, classMap, columnTypeMap, sql, array);
	}

	public <T> List<T> list(Class<T> clazz, String sql, Object... array) {
		return list(clazz, null, null, sql, array);
	}

	public <T> T find(Class<T> clazz, Map<Class, Class> classMap, Map<String, Class> columnTypeMap, String sql, Object... array) {
		List<T> list = read(false, clazz, classMap, columnTypeMap, sql, array);
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public <T> T find(Class<T> clazz, String sql, Object... array) {
		return find(clazz, null, null, sql, array);
	}

}
