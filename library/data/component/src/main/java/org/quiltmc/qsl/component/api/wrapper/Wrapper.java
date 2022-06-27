package org.quiltmc.qsl.component.api.wrapper;

public class Wrapper<T> {
	private T value;

	public Wrapper(T value) {
		this.value = value;
	}

	public void set(T value) {
		this.value = value;
	}

	public T get() {
		return this.value;
	}
}
