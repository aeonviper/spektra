package omega.service;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultData {

	protected ResultSet resultSet;

	public ResultData(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	protected <T> T nullify(String name, T value) throws SQLException {
		if (resultSet.getObject(name) == null) {
			return null;
		} else {
			return value;
		}
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public Long getLong(String name) throws SQLException {
		return nullify(name, resultSet.getLong(name));
	}

	public Integer getInt(String name) throws SQLException {
		return nullify(name, resultSet.getInt(name));
	}

	public String getString(String name) throws SQLException {
		return resultSet.getString(name);
	}

	public <T> T getObject(String name, Class<T> clazz) throws SQLException {
		return resultSet.getObject(name, clazz);
	}

	public Object getObject(String name) throws SQLException {
		return resultSet.getObject(name);
	}

}
