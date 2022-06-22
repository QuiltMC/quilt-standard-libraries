package org.quiltmc.qsl.component.api;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.predicates.ClassInjectionPredicate;
import org.quiltmc.qsl.component.impl.predicates.FilteredInheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.predicates.InheritedInjectionPredicate;

import java.util.Map;
import java.util.Optional;

public final class Components {
	public static <C extends Component> void inject(Class<?> clazz, ComponentIdentifier<C> component) {
		ComponentsImpl.inject(new ClassInjectionPredicate(clazz), component);
	}

	public static <C extends Component> void injectInheritage(Class<?> clazz, ComponentIdentifier<C> component) {
		ComponentsImpl.inject(new InheritedInjectionPredicate(clazz), component);
	}

	public static <C extends Component> void injectInheritanceExcept(Class<?> clazz, ComponentIdentifier<C> component, Class<?>... exceptions) {
		ComponentsImpl.inject(new FilteredInheritedInjectionPredicate(clazz, exceptions), component);
	}

	public static <T extends Component, S> Optional<T> expose(ComponentIdentifier<T> id, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.getContainer().expose(id.id())
					.map(id::cast)
					.map(Optional::orElseThrow); // If the casting fails something is wrong with the provided ComponentIdentifier. In that case we just throw.
		}

		return Optional.empty();
	}

	public static <S> Map<Identifier, Component> exposeAll(S obj) {
		return obj instanceof ComponentProvider provider ? provider.getContainer().exposeAll() : ImmutableMap.of();
	}
}
