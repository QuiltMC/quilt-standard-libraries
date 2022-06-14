package org.quiltmc.qsl.component.impl.predicates;

import org.quiltmc.qsl.component.api.ComponentProvider;

import java.util.Objects;

public class InheritedInjectionPredicate extends ClassInjectionPredicate {
	public InheritedInjectionPredicate(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public boolean canInject(ComponentProvider provider) {
		return this.canInject(provider.getClass());
	}

	public boolean canInject(Class<?> current) {
		return this.clazz == current || (current != null && canInject(current.getSuperclass()));
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.getClass());
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof InheritedInjectionPredicate && super.equals(o);
	}

	@Override
	public String toString() {
		return "InheritedInjectionPredicate{clazz=" + this.clazz + '}';
	}
}
