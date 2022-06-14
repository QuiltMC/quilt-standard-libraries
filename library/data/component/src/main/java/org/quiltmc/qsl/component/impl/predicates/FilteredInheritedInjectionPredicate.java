package org.quiltmc.qsl.component.impl.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FilteredInheritedInjectionPredicate extends InheritedInjectionPredicate {
	private final List<Class<?>> exceptions;

	public FilteredInheritedInjectionPredicate(Class<?> clazz, Class<?>[] exceptions) {
		super(clazz);
		this.exceptions = new ArrayList<>(Arrays.asList(exceptions));
	}

	@Override
	public boolean canInject(Class<?> current) {
		return !this.exceptions.contains(current) && super.canInject(current);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 37 * Objects.hash(this.exceptions);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FilteredInheritedInjectionPredicate)) return false;
		if (!super.equals(o)) return false;
		FilteredInheritedInjectionPredicate that = (FilteredInheritedInjectionPredicate) o;
		return exceptions.equals(that.exceptions);
	}

	@Override
	public String toString() {
		return "FilteredInheritedInjectionPredicate{clazz=" + clazz + ", exceptions=" + exceptions + '}';
	}
}
