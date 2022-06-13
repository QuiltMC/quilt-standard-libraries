package org.quiltmc.qsl.component.impl.predicates;

import org.quiltmc.qsl.component.api.ComponentProvider;

import java.util.Objects;

public class InheritedInjectionPredicate extends ClassInjectionPredicate {
	public InheritedInjectionPredicate(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public boolean canInject(ComponentProvider provider) {
		return canInject(this.clazz, provider.getClass());
	}

	public static boolean canInject(Class<?> target, Class<?> current) {
		return target == current || (current != null && canInject(target, current.getSuperclass()));
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.getClass());
	}

	@Override
	public boolean equals(Object o) {
		return o.getClass() == InheritedInjectionPredicate.class && super.equals(o);
	}
}
