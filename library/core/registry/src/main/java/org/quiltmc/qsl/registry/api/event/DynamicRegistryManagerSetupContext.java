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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * Represents the {@link DynamicRegistryManager} setup context provided in the {@link RegistryEvents#DYNAMIC_REGISTRY_SETUP} event.
 */
@ApiStatus.NonExtendable
public interface DynamicRegistryManagerSetupContext {
	/**
	 * {@return the dynamic registry manager that is being currently setup}
	 */
	@Contract(pure = true)
	@NotNull DynamicRegistryManager registryManager();

	/**
	 * {@return the resource manager that is used to setup the dynamic registries}
	 */
	@Contract(pure = true)
	@NotNull ResourceManager resourceManager();

	/**
	 * Attempts to safely register a game object into the given registry.
	 * <p>
	 * This method is preferred instead of {@link Registry#register(Registry, Identifier, Object)}
	 * as it makes sure to not overwrite data-pack-provided entries, it also makes sure the registry exists.
	 *
	 * @param registryKey        the key of the registry to register into
	 * @param id                 the identifier of the game object to register
	 * @param gameObjectSupplier the supplier of the game object to register
	 * @param <V>                the type of game object to register
	 * @return the optional game object, if the registry is present then the optional is filled, or empty otherwise
	 */
	default <V> @NotNull Optional<V> register(@NotNull RegistryKey<? extends Registry<V>> registryKey, @NotNull Identifier id,
			@NotNull Supplier<V> gameObjectSupplier) {
		return this.registryManager().getOptional(registryKey)
				.map(registry -> {
					if (registry.containsId(id)) {
						return registry.get(id);
					} else {
						return Registry.register(registry, id, gameObjectSupplier.get());
					}
				});
	}

	/**
	 * Gets the registries requested by their keys.
	 * <p>
	 * If one of the queried registries isn't found, then this method will return {@code null}.
	 *
	 * @param registryKeys the keys of the registries to get
	 * @return the registry map if all the queried registries have been found, or {@code null} otherwise
	 */
	@Contract(pure = true)
	default @Nullable RegistryMap getRegistries(@NotNull Set<RegistryKey<? extends Registry<?>>> registryKeys) {
		if (registryKeys.size() == 0) throw new IllegalArgumentException("Please provide at least one registry to gather.");

		Map<RegistryKey<? extends Registry<?>>, Registry<?>> foundRegistries = null;

		for (var key : registryKeys) {
			var maybe = this.registryManager().getOptional(key);

			if (maybe.isPresent()) {
				if (foundRegistries == null) {
					foundRegistries = new Reference2ObjectOpenHashMap<>();
				}

				foundRegistries.put(key, maybe.get());
			}
		}

		if (foundRegistries == null || foundRegistries.size() != registryKeys.size()) {
			return null;
		}

		return new RegistryMap(foundRegistries);
	}

	/**
	 * Executes the given action if all the provided registry keys are present in the {@link DynamicRegistryManager}.
	 *
	 * @param action       the action
	 * @param registryKeys the registry keys to check
	 */
	default void withRegistries(@NotNull Consumer<RegistryMap> action, @NotNull Set<RegistryKey<? extends Registry<?>>> registryKeys) {
		var registries = this.getRegistries(registryKeys);

		if (registries != null) {
			action.accept(registries);
		}
	}

	/**
	 * Attempts to create a new registry monitor for the given registry.
	 *
	 * @param registryKey the key of the registry to monitor
	 * @param action      the monitor callback
	 * @param <V>         the type of values held in the registry
	 */
	default <V> void monitor(RegistryKey<? extends Registry<V>> registryKey, Consumer<RegistryMonitor<V>> action) {
		this.registryManager().getOptional(registryKey).ifPresent(registry -> {
			action.accept(RegistryMonitor.create(registry));
		});
	}

	/**
	 * Represents a map of known registries.
	 *
	 * @param registries the map of registries
	 */
	record RegistryMap(Map<RegistryKey<? extends Registry<?>>, Registry<?>> registries) {
		/**
		 * Gets the registry from its key in this map.
		 *
		 * @param registryKey the key of the registry
		 * @param <V>         the type of values held in the registry
		 * @return the registry if present, or {@code null} otherwise
		 */
		@Contract(pure = true)
		@SuppressWarnings("unchecked")
		public <V> Registry<V> get(RegistryKey<? extends Registry<V>> registryKey) {
			return (Registry<V>) this.registries.get(registryKey);
		}

		/**
		 * Registers the given game object into the given registry.
		 *
		 * @param registryKey the key of the registry to register into
		 * @param id          the identifier of the game object to register
		 * @param gameObject  the game object to register
		 * @param <V>         the type of values held in the registry
		 * @return the game object
		 */
		public <V> @NotNull V register(@NotNull RegistryKey<? extends Registry<V>> registryKey, @NotNull Identifier id,
				@NotNull V gameObject) {
			return Registry.register(this.get(registryKey), id, gameObject);
		}
	}
}
