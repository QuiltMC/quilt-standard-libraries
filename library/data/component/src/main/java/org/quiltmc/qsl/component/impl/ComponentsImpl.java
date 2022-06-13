package org.quiltmc.qsl.component.impl;

import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentInjectionPredicate;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;

import java.util.*;
import java.util.function.Supplier;

public class ComponentsImpl {
	private static final Map<ComponentInjectionPredicate, Map<Identifier, Supplier<? extends Component>>> INJECTION_REGISTRY = new HashMap<>();
	private static final Map<Identifier, Supplier<? extends Component>> REGISTRY  = new HashMap<>();

	public static <C extends Component> void inject(ComponentInjectionPredicate predicate, ComponentIdentifier<C> id) {
		Map<Identifier, Supplier<? extends Component>> currentInjections = INJECTION_REGISTRY.getOrDefault(predicate, new HashMap<>());
		Supplier<? extends Component> supplier = REGISTRY.get(id.id());
		if (supplier == null) {
			throw new IllegalArgumentException("The target id %s does not match any registered component!".formatted(id.toString()));
		}

		currentInjections.put(id.id(), supplier);

		INJECTION_REGISTRY.put(predicate, currentInjections);
	}

	public static <T extends Component> ComponentIdentifier<T> register(Identifier id, Supplier<T> component) {
		REGISTRY.put(id, component);
		return new ComponentIdentifier<>(id);
	}

	public static Map<Identifier, Supplier<? extends Component>> get(ComponentProvider provider) {
		return INJECTION_REGISTRY.entrySet().stream()
				.filter(it -> it.getKey().canInject(provider))
				.collect(HashMap::new, (map, entry) -> map.putAll(entry.getValue()), HashMap::putAll);
	}
}
