/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.component.impl.injection.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.util.Util;

import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.injection.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

public abstract class InjectionManager<P extends InjectionPredicate, I> {
	private final Map<P, List<ComponentEntry<?>>> injections = new HashMap<>();
	private final Map<Class<?>, List<I>> cache = new IdentityHashMap<>();

	// When a type collides, one of the two variants will override the other one.
	// That would mean only the last added type, will ever be properly processed.
	public void inject(P predicate, ComponentEntry<?>... entries) {
		for (ComponentEntry<?> entry : entries) {
			var type = entry.type();

			if (Components.REGISTRY.get(type.id()) == null) {
				throw ErrorUtil.illegalArgument("The target id %s does not match any registered component", type).get();
			}
		}

		var targetBucket = this.injections.computeIfAbsent(predicate, p -> new ArrayList<>());
		Collections.addAll(targetBucket, entries);

		this.cache.clear();
	}

	public abstract List<ComponentEntry<?>> getInjections(ComponentProvider provider);

	protected Stream<Map.Entry<P, List<ComponentEntry<?>>>> streamEntries() {
		return this.injections.entrySet().stream();
	}

	protected Maybe<List<I>> getCache(Class<?> clazz) {
		if (!this.cache.containsKey(clazz)) {
			return Maybe.nothing();
		}

		return Maybe.just(this.cache.get(clazz));
	}

	protected void record(Class<?> providerClass, List<I> injections) {
		if (this.cache.put(providerClass, Util.make(new ArrayList<>(), list -> list.addAll(injections))) != null) {
			// If there was a value there, it means we attempted an override, and so we throw.
			throw ErrorUtil.illegalArgument("Cannot register cache twice for class %s", providerClass).get();
		}
	}
}
