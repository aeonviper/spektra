package omega.service;

public class Specification<T> {

	String qualifier;
	Class<T> clazz;

	public Specification(String qualifier, Class<T> clazz) {
		this.qualifier = qualifier;
		this.clazz = clazz;
	}

	public String getQualifier() {
		return qualifier;
	}

	public Class<T> getClazz() {
		return clazz;
	}

}

