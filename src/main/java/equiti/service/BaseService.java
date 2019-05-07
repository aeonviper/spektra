package equiti.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.core.BeanUtility;
import omega.service.GenericService;

public class BaseService extends GenericService {

	@Transactional(type = TransactionType.READWRITE)
	public <T> int insert(String tableName, String sequenceName, T entity, String... array) {
		String sql = "insert into " + tableName + " (id," + join(array, ",") + ") values (nextval('" + sequenceName + "')," + repeat("?", array.length, ",") + ")";
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
	public Long sequence(String name) {
		return select(Long.class, "select nextval('" + name + "')");
	}

}
