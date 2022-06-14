package org.quiltmc.qsl.component.api;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.predicates.ClassInjectionPredicate;
import org.quiltmc.qsl.component.impl.predicates.FilteredInheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.predicates.InheritedInjectionPredicate;

import java.util.Optional;

public final class Components {
	public static <T, C extends Component> void inject(Class<T> clazz, ComponentIdentifier<C> component) {
		ComponentsImpl.inject(new ClassInjectionPredicate(clazz), component);
	}

	public static <T, C extends Component> void injectInheritage(Class<T> clazz, ComponentIdentifier<C> component) {
		ComponentsImpl.inject(new InheritedInjectionPredicate(clazz), component);
	}

	public static <T, C extends Component> void injectInheritanceExcept(Class<?> clazz, ComponentIdentifier<C> component, Class<?>... exceptions) {
		ComponentsImpl.inject(new FilteredInheritedInjectionPredicate(clazz, exceptions), component);
	}

	public static <T extends Component, S> Optional<T> expose(ComponentIdentifier<T> id, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.expose(id)
					.map(id::cast)
					.map(Optional::orElseThrow);
		}

		return Optional.empty();
	}

	public static <S> ImmutableCollection<Component> exposeAll(S obj) {
		return obj instanceof ComponentProvider provider ? provider.exposeAll() : ImmutableList.of();
	}
}
