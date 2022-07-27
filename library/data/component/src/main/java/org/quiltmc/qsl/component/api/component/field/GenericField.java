package org.quiltmc.qsl.component.api.component.field;

public interface GenericField<T> {
	T getValue();

	void setValue(T value);
}
