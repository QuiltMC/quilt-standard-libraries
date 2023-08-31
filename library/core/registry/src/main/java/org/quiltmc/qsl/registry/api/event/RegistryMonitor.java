/*
 * Copyright 2022 The Quilt Project
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

import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.event.RegistryMonitorImpl;

/**
 * A higher level tool for monitoring the manipulation of Minecraft's content registries.
 *
 * @param <V> the entry type of the monitored {@link Registry}
 * @see RegistryEvents RegistryEvents for a low-level API
 */
@ApiStatus.NonExtendable
public interface RegistryMonitor<V> {
	/**
	 * A builder-like method to append a filter to the current registry monitor, for determining what entries to invoke
	 * registered callbacks for.
	 *
	 * @param filter a predicate which determines what entries to invoke callbacks for
	 * @return the current registry monitor object, so as to allow chaining other methods in builder-like fashion
	 */
	RegistryMonitor<V> filter(Predicate<RegistryEntryContext<V>> filter);

	/**
	 * Registers the specified callback to be invoked for <b>every entry ever</b> to be registered in the monitor's registry.
	 * <p>
	 * Entries must also match the monitor's filters.
	 * <p>
	 * Registration to the registry being iterated must use the {@link RegistryEntryContext#register(Identifier, Object)} method inside the callback,
	 * or alternatively use the {@link RegistryEntryContext#registry()} method to get the registry instance,
	 * for example: {@code context.register(id, block);}.
	 *
	 * @param callback the callback to be invoked on entries
	 */
	void forAll(RegistryEvents.EntryAdded<V> callback);

	/**
	 * Registers the specified callback to be invoked for <i>all future entries</i> to registered in the monitor's registry.
	 * <p>
	 * Entries must also match the monitor's filters.
	 *
	 * @param callback the callback to be invoked on entries
	 */
	void forUpcoming(RegistryEvents.EntryAdded<V> callback);

	/**
	 * Creates a new {@link RegistryMonitor} to monitor the specified {@link Registry}.
	 *
	 * @param registry the {@link Registry} to monitor
	 * @param <V>      the entry type of the {@link Registry} being monitored
	 * @return a new {@link RegistryMonitor} monitoring the specified {@link Registry}
	 */
	static <V> RegistryMonitor<V> create(Registry<V> registry) {
		return new RegistryMonitorImpl<>(registry);
	}
}
