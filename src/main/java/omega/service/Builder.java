package omega.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import omega.core.BeanUtility;
import omega.core.Core;

public class Builder<T> {

	public T build(ResultData rd) throws SQLException {
		return null;
	}
	
	public T build(ResultSet rs) throws SQLException {
		return build(new ResultData(rs));
	}

	public static Builder<Object[]> build(Specification[] specificationArray) {
		return new Builder<Object[]>() {
			public Object[] build(ResultSet rs) throws SQLException {
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();
				List list = new ArrayList<>();
				for (Specification specification : specificationArray) {
					String qualifier = specification.getQualifier();
					Class clazz = specification.getClazz();
					Object entity = Core.getInjector().getInstance(clazz);
					list.add(entity);
					try {
						for (int column = 1; column <= columnCount; column++) {
							String columnName = metaData.getColumnName(column);
							if (columnName.startsWith(qualifier)) {
								BeanUtility.instance().setProperty(entity, columnName.substring(qualifier.length()), rs.getObject(column));
							}
						}
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
				return list.toArray();
			}
		};
	}

}
