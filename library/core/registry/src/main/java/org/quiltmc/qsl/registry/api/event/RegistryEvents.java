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

package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.impl.event.RegistryEventStorage;

/**
 * Events for listening to the manipulation of Minecraft's content registries.
 * <p>
 * The events are to be used for very low-level purposes, and callbacks are only called on registry manipulations
 * occurring after the event registration. This means that mod load order can affect what is picked up by these events.
 * <p>
 * For more high-level monitoring of registries, including methods to ease the inconvenience of mod load order,
 * use {@link RegistryMonitor}.
 */
public final class RegistryEvents {
	private RegistryEvents() {
	}

	/**
	 * Gets the entry added event for a specific Minecraft registry.
	 * <p>
	 * The event is invoked upon the addition or assignment of an entry in the specified registry.
	 *
	 * @param registry the {@link Registry} for this event to listen for
	 * @param <V>      the entry type of the {@link Registry} to listen for
	 * @return the entry added event for the specified registry, which can have callbacks registered to it
	 */
	public static <V> Event<EntryAdded<V>> getEntryAddEvent(Registry<V> registry) {
		return RegistryEventStorage.as(registry).quilt$getEntryAddedEvent();
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #getEntryAddEvent(Registry)}.
	 *
	 * @param <V> the entry type of the {@link Registry} being listened for
	 * @see #getEntryAddEvent(Registry)
	 */
	@FunctionalInterface
	public interface EntryAdded<V> {
		/**
		 * Called when an entry in this callback's event's {@link Registry} has an entry added or assigned.
		 *
		 * @param context an object containing information regarding the registry, entry object, and ID of the entry
		 *                being registered
		 */
		void onAdded(RegistryEntryContext<V> context);
	}
}
