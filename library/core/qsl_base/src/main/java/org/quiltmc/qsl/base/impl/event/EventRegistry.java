/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.base.impl.event;

import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareEntrypoint;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareEntrypoint;
import org.quiltmc.qsl.base.api.event.server.DedicatedServerEventAwareEntrypoint;

@ApiStatus.Internal
public final class EventRegistry {
	private static final EventSideTarget[] TARGETS = EventSideTarget.values();

	private EventRegistry() {
		throw new UnsupportedOperationException("EventRegistry only contains static definitions.");
	}

	@SuppressWarnings("unchecked")
	public static <T> void register(Event<T> event, Class<? super T> type) {
		for (var target : TARGETS) {
			// Search if the event callback qualifies to be converted into an entrypoint.
			if (target.entrypointClass().isAssignableFrom(type)) {
				List<?> entrypoints = FabricLoader.getInstance().getEntrypoints(target.entrypointKey(), target.entrypointClass());

				// Search for matching entrypoint.
				for (Object entrypoint : entrypoints) {
					// Searching if the given entrypoint is a listener of the event being registered.
					if (type.isAssignableFrom(entrypoint.getClass())) {
						// It is, then register the listener.
						((Event) event).register(entrypoint);
					}
				}

				break;
			}
		}
	}

	private enum EventSideTarget {
		CLIENT("client_events", ClientEventAwareEntrypoint.class),
		COMMON("events", EventAwareEntrypoint.class),
		DEDICATED_SERVER("server_events", DedicatedServerEventAwareEntrypoint.class);

		private final String entrypointKey;
		private final Class<?> entrypoint;

		EventSideTarget(String entrypointKey, Class<?> entrypoint) {
			this.entrypointKey = entrypointKey;
			this.entrypoint = entrypoint;
		}

		public String entrypointKey() {
			return this.entrypointKey;
		}

		public Class<?> entrypointClass() {
			return this.entrypoint;
		}
	}
}
