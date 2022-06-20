package org.quiltmc.qsl.component.impl.defaults;

import org.quiltmc.qsl.component.api.components.IntegerComponent;

import java.util.Objects;

public class DefaultIntegerComponent implements IntegerComponent {
	private int value;

	public DefaultIntegerComponent(int defaultValue) {
		this.value = defaultValue;
	}

	public DefaultIntegerComponent() {
		this(0);
	}

	@Override
	public int get() {
		return this.value;
	}

	@Override
	public void set(int value) {
		this.value = value;
	}

	@Override
	public void increment() {
		this.value++;
	}

	@Override
	public void decrement() {
		this.value--;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DefaultIntegerComponent that)) return false;
		return value == that.value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}
