package org.quiltmc.qsl.component.impl.predicates;

import org.quiltmc.qsl.component.api.ComponentInjectionPredicate;
import org.quiltmc.qsl.component.api.ComponentProvider;

import java.util.Arrays;
import java.util.Objects;

public class ClassInjectionPredicate implements ComponentInjectionPredicate {
	protected final Class<?> clazz;

	public ClassInjectionPredicate(Class<?> clazz) {
		if (implementsComponentProvider(clazz)) {
			this.clazz = clazz;
		} else {
			throw new IllegalArgumentException("Cannot create an injection predicate for a class that isn't a ComponentProvider");
		}
	}

	private static boolean implementsComponentProvider(Class<?> clazz) {
		return Arrays.stream(clazz.getInterfaces()).anyMatch(it -> it == ComponentProvider.class) || (clazz.getSuperclass() != null && implementsComponentProvider(clazz.getSuperclass()));
	}

	@Override
	public boolean canInject(ComponentProvider provider) {
		return provider.getClass() == clazz;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof ClassInjectionPredicate that) {
			return clazz.equals(that.clazz);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(clazz);
	}

	@Override
	public String toString() {
		return "ClassInjectionPredicate{clazz=" + clazz + '}';
	}
}
