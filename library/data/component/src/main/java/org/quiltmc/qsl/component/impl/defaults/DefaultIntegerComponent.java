package org.quiltmc.qsl.component.impl.defaults;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.components.IntegerComponent;

import java.util.Objects;

public class DefaultIntegerComponent implements IntegerComponent {

	private int value;
	@Nullable
	private Runnable saveOperation;

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
		this.saveNeeded();
	}

	@Override
	public void increment() {
		this.value++;
		this.saveNeeded();
	}

	@Override
	public void decrement() {
		this.value--;
		this.saveNeeded();
	}

	// TODO: Fix hashing and equals
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

	@Override
	public void saveNeeded() {
		if (this.saveOperation != null) {
			this.saveOperation.run();
		}
	}

	@Override
	public void setSaveOperation(@Nullable Runnable runnable) {
		this.saveOperation = runnable;
	}
}
