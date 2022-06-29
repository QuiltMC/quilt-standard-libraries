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

package org.quiltmc.qsl.component.api.event;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;

public class ComponentEvents {

	public static final Event<DynamicInject> DYNAMIC_INJECT = Event.create(DynamicInject.class, listeners -> (provider, injector) -> {
		for (DynamicInject listener : listeners) {
			listener.onInject(provider, injector);
		}
	});

	@FunctionalInterface
	public interface DynamicInject {
		void onInject(ComponentProvider provider, Injector injector);
	}

	@FunctionalInterface
	public interface Injector {
		default void injectIf(boolean condition, ComponentType<?> type) {
			if (condition) {
				this.inject(type);
			}
		}

		void inject(ComponentType<?> type);
	}
}
