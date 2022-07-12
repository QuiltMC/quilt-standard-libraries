package org.quiltmc.qsl.component.impl.injection.predicate.dynamic;

import org.quiltmc.qsl.component.api.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.predicate.DynamicInjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;

import java.util.function.Predicate;

public class DynamicWrappedPredicate<P extends ComponentProvider> implements DynamicInjectionPredicate {
	private final InjectionPredicate wrapped;
	private final Predicate<P> predicate;

	public DynamicWrappedPredicate(InjectionPredicate wrapped, Predicate<P> predicate) {
		this.wrapped = wrapped;
		this.predicate = predicate;
	}

	@Override
	public boolean isClassValid(Class<?> clazz) {
		return this.wrapped.isClassValid(clazz);
	}

	@Override
	public boolean canInject(ComponentProvider provider) {
		return this.predicate.test((P) provider);
	}
}
