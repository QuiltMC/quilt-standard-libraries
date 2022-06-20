package org.quiltmc.qsl.component.impl;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentInjectionPredicate;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;

import java.util.*;
import java.util.function.Supplier;

public class ComponentsImpl {
	private static final Map<ComponentInjectionPredicate, Set<Identifier>> INJECTION_REGISTRY = new HashMap<>();
	private static final Map<Identifier, Supplier<? extends Component>> REGISTRY = new HashMap<>();

	public static <C extends Component> void inject(ComponentInjectionPredicate predicate, ComponentIdentifier<C> id) {
		Supplier<? extends Component> supplier = REGISTRY.get(id.id());
		if (supplier == null) {
			throw new IllegalArgumentException(
					"The target id %s does not match any registered component!".formatted(id.toString())
			);
		}

		if (INJECTION_REGISTRY.containsKey(predicate)) {
			if (!INJECTION_REGISTRY.get(predicate).add(id.id())) {
				throw new IllegalStateException(
						"Cannot inject the predicate %s with %s more than once! Consider creating a new component!"
								.formatted(predicate.toString(), id.id().toString())
				);
			}
		} else {
			INJECTION_REGISTRY.put(predicate, Util.make(new HashSet<>(), set -> set.add(id.id())));
		}
		ComponentCache.getInstance().clear(); // Always clear the cache after an injection is registered.
	}

	public static <T extends Component> ComponentIdentifier<T> register(Identifier id, Supplier<T> component) {
		REGISTRY.put(id, component);
		return new ComponentIdentifier<>(id);
	}

	public static Map<Identifier, Supplier<? extends Component>> get(ComponentProvider provider) {
		return ComponentCache.getInstance().getCache(provider.getClass()).orElseGet(() -> {
			Map<Identifier, Supplier<? extends Component>> returnMap = INJECTION_REGISTRY.entrySet().stream()
				.filter(it -> it.getKey().canInject(provider))
				.map(Map.Entry::getValue)
				.collect(HashMap::new, (map, ids) -> ids.forEach(id -> map.put(id, REGISTRY.get(id))), HashMap::putAll);

			ComponentCache.getInstance().record(provider.getClass(), returnMap);

			return returnMap;
		});
	}
}
