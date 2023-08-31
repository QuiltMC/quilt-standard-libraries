/*
 * Copyright 2021 The Quilt Project
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModInternal;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;
import org.quiltmc.qsl.base.api.event.server.DedicatedServerEventAwareListener;

@ModInternal
@ApiStatus.Internal
public final class EventRegistry implements ModInitializer {
	private static List<Event<?>> pendingEventsRegistration = new ArrayList<>();
	private static boolean initialized = false;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void listenAll(Object listener, Event<?>... events) {
		var listenedPhases = getListenedPhases(listener.getClass());

		// Check whether we actually can register stuff. We only commit the registration if all events can.
		for (var event : events) {
			if (!event.getType().isAssignableFrom(listener.getClass())) {
				throw new IllegalArgumentException("Given object " + listener + " is not a listener of event " + event);
			}

			if (event.getType().getTypeParameters().length > 0) {
				throw new IllegalArgumentException("Cannot register a listener for the event " + event + " which is using generic parameters with listenAll.");
			}

			listenedPhases.putIfAbsent(event.getType(), Event.DEFAULT_PHASE);
		}

		// We can register, so we do!
		for (var event : events) {
			((Event) event).register(listenedPhases.get(event.getType()), listener);
		}
	}

	private static Map<Class<?>, Identifier> getListenedPhases(Class<?> listenerClass) {
		var map = new Object2ObjectOpenHashMap<Class<?>, Identifier>();

		for (var annotation : listenerClass.getAnnotations()) {
			if (annotation instanceof ListenerPhase phase) {
				map.put(phase.callbackTarget(), new Identifier(phase.namespace(), phase.path()));
			}
		}

		return map;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> void register(Event<T> event) {
		if (!initialized) {
			pendingEventsRegistration.add(event);
			return;
		}

		for (var target : EventSideTarget.VALUES) {
			// Search if the callback qualifies is unique to this event.
			if (target.listenerClass().isAssignableFrom(event.getType())) {
				List<?> entrypoints = QuiltLoader.getEntrypoints(target.entrypointKey(), target.listenerClass());

				// Search for matching entrypoint.
				for (Object entrypoint : entrypoints) {
					var listenedPhases = getListenedPhases(entrypoint.getClass());

					// Searching if the given entrypoint is a listener of the event being registered.
					if (event.getType().isAssignableFrom(entrypoint.getClass())) {
						// It is, then register the listener.
						var phase = listenedPhases.getOrDefault(event.getType(), Event.DEFAULT_PHASE);
						((Event) event).register(phase, entrypoint);
					}
				}

				break;
			}
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		initialized = true;

		for (var event : pendingEventsRegistration) {
			register(event);
		}

		pendingEventsRegistration = null;
	}

	enum EventSideTarget {
		CLIENT("client_events", ClientEventAwareListener.class),
		COMMON("events", EventAwareListener.class),
		DEDICATED_SERVER("server_events", DedicatedServerEventAwareListener.class);

		public static final List<EventSideTarget> VALUES = List.of(values());

		private final String entrypointKey;
		private final Class<?> listenerClass;

		EventSideTarget(String entrypointKey, Class<?> listenerClass) {
			this.entrypointKey = entrypointKey;
			this.listenerClass = listenerClass;
		}

		public String entrypointKey() {
			return this.entrypointKey;
		}

		public Class<?> listenerClass() {
			return this.listenerClass;
		}
	}
}
