package equiti.core;

public class Container<T> {

	public T value;

	public Container() {
	}

	public Container(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

}
