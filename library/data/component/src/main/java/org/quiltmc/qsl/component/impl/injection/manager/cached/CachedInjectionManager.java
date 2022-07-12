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

package org.quiltmc.qsl.component.impl.injection.manager.cached;

import org.quiltmc.qsl.component.api.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.injection.manager.InjectionManager;

import java.util.List;
import java.util.Objects;

public class CachedInjectionManager extends InjectionManager<InjectionPredicate, ComponentEntry<?>> {
	@Override
	public List<ComponentEntry<?>> getInjections(ComponentProvider provider) {
		Class<? extends ComponentProvider> providerClass = provider.getClass();
		return this.getCache(providerClass).unwrapOrGet(() -> {
			List<ComponentEntry<?>> componentEntries = this.initInjections(providerClass);
			this.record(providerClass, componentEntries);
			return componentEntries;
		});
	}

	private List<ComponentEntry<?>> initInjections(Class<? extends ComponentProvider> providerClass) {
		return this.streamEntries()
				.map(entry -> entry.getKey().isClassValid(providerClass) ? entry.getValue() : null)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.toList();
	}
}
