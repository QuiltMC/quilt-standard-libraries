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

package org.quiltmc.qsl.component.impl.injection.manager.dynamic;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.quiltmc.qsl.component.api.injection.predicate.DynamicInjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.injection.manager.InjectionManager;

public class DynamicInjectionManager extends InjectionManager<DynamicInjectionPredicate, DynamicInjectionManager.DynamicInjection> {
	@Override
	public List<ComponentEntry<?>> getInjections(ComponentProvider provider) {
		Class<? extends ComponentProvider> providerClass = provider.getClass();
		// First check the cached side
		return this.getCache(providerClass).or(() -> {
			var injections = this.initInjections(providerClass);
			this.record(providerClass, injections);
			return Optional.of(injections);
		}).orElseGet(List::of).stream()
				.map(dynamicInjection -> dynamicInjection.test(provider) ? dynamicInjection.componentEntries() : null) // After check the dynamic side
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private List<DynamicInjection> initInjections(Class<?> clazz) {
		return this.streamEntries()
				.filter(entry -> entry.getKey().isClassValid(clazz))
				.map(entry -> new DynamicInjection(entry.getKey(), entry.getValue()))
				.toList();
	}

	public record DynamicInjection(DynamicInjectionPredicate predicate,
								List<ComponentEntry<?>> componentEntries) implements Predicate<ComponentProvider> {
		@Override
		public boolean test(ComponentProvider provider) {
			return this.predicate.canInject(provider);
		}
	}
}
