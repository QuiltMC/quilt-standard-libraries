package org.quiltmc.qsl.component.impl;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.quiltmc.qsl.component.api.Component;

import java.util.*;
import java.util.function.Supplier;

public class ComponentCache {

	private static ComponentCache INSTANCE;

	private final Map<Class<?>, Map<Identifier, Supplier<? extends Component>>> injectionCache = new HashMap<>();

	private ComponentCache() {

	}

	public static ComponentCache getInstance(){
		if (INSTANCE == null) {
			INSTANCE = new ComponentCache();
		}

		return INSTANCE;
	}

	public Optional<Map<Identifier, Supplier<? extends Component>>> getCache(Class<?> clazz) {
		if (!this.injectionCache.containsKey(clazz)) {
			return Optional.empty();
		}

		return Optional.of(this.injectionCache.get(clazz));
	}

	public void clear() {
		this.injectionCache.clear();
	}

	public void record(Class<?> clazz, Map<Identifier, Supplier<? extends Component>> components) {
		if (this.injectionCache.put(clazz, Util.make(new HashMap<>(), map -> map.putAll(components))) != null) {
			// If there was a value there, it means we attempted an override, and so we throw.
			throw new IllegalStateException("Cannot register cache twice for class %s".formatted(clazz));
		}
	}
}
