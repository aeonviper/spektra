package omega.service;

import java.util.List;

public class Decorator<T> {

	public T decorate(T entity) {
		return entity;
	}

	public List<T> decorate(List<T> entityList) {
		for (T entity : entityList) {
			decorate(entity);
		}
		return entityList;
	}

}
