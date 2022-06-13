package org.quiltmc.qsl.component.impl.defaults;

import org.quiltmc.qsl.component.api.components.IntegerComponent;

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
}
