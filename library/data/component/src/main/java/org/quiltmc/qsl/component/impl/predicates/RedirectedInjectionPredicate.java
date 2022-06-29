package org.quiltmc.qsl.component.impl.predicates;

import org.quiltmc.qsl.component.api.ComponentProvider;

import java.util.Set;

public class RedirectedInjectionPredicate extends ClassInjectionPredicate {
	private final Set<Class<?>> redirections;

	public RedirectedInjectionPredicate(Class<?> clazz, Set<Class<?>> redirections) {
		super(clazz);
		this.redirections = redirections;
	}

	@Override
	public boolean canInject(ComponentProvider provider) {
		return super.canInject(provider) || this.redirections.contains(provider.getClass());
	}
}
