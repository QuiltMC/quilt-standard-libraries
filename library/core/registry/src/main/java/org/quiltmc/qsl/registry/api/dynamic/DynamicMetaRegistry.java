/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.api.dynamic;

import com.mojang.serialization.Codec;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.dynamic.DynamicMetaRegistryImpl;

/**
 * Allows registration of dynamic registries for use through {@link net.minecraft.registry.DynamicRegistryManager}.
 * <p>
 * Dynamic registries are only available in a world context. Entries must be added either:
 * <ul>
 * <li>as datapack files
 * <li>through the {@link org.quiltmc.qsl.registry.api.event.RegistryEvents#DYNAMIC_REGISTRY_SETUP dynamic registry setup event}
 * </ul>
 * <p>
 * This registry will be frozen at the same time as static registries.
 */
public final class DynamicMetaRegistry {
	/**
	 * Registers a server-side dynamic registry.
	 * <p>
	 * Entries will be loaded from {@code "data/<namespace>/<registry_namespace>/<registry_path>"} for every datapack
	 * {@code namespace}, where {@code registry_namespace} and {@code registry_path}'s values are respectively
	 * {@code key.getLocation().getNamespace()} and {@code key.getLocation().getPath()}.
	 *
	 * @param <E>        the type of elements in the dynamic registry
	 * @param key        a {@link RegistryKey#ofRegistry(Identifier) key for the new dynamic registry}
	 * @param entryCodec the codec used to deserialize entries from datapacks
	 * @throws IllegalStateException if this registry of registries already got frozen
	 */
	public static <E> void register(RegistryKey<? extends Registry<E>> key, Codec<E> entryCodec, DynamicRegistryFlag... flags) {
		DynamicMetaRegistryImpl.register(key, entryCodec, flags);
	}

	/**
	 * Registers a dynamic registry which contents get synced between the server and connected clients.
	 * <p>
	 * Entries will be loaded from {@code "data/<namespace>/<registry_namespace>/<registry_path>"} for every datapack
	 * {@code namespace}, where {@code registry_namespace} and {@code registry_path}'s values are respectively
	 * {@code key.getLocation().getNamespace()} and {@code key.getLocation().getPath()}.
	 *
	 * @param <E>        the type of elements in the dynamic registry
	 * @param key        a {@link RegistryKey#ofRegistry(Identifier) key for the new dynamic registry}
	 * @param entryCodec the codec used to both deserialize entries from datapacks and (de)serialize entries to and from packets
	 * @throws IllegalStateException if this registry of registries already got frozen
	 * @see #registerSynced(RegistryKey, Codec, Codec, DynamicRegistryFlag...)
	 */
	public static <E> void registerSynced(RegistryKey<? extends Registry<E>> key, Codec<E> entryCodec, DynamicRegistryFlag... flags) {
		DynamicMetaRegistryImpl.registerSynced(key, entryCodec, entryCodec, flags);
	}

	/**
	 * Registers a dynamic registry which contents get synced between the server and connected clients.
	 * <p>
	 * Entries will be loaded from {@code "data/<namespace>/<registry_namespace>/<registry_path>"} for every datapack
	 * {@code namespace}, where {@code registry_namespace} and {@code registry_path}'s values are respectively
	 * {@code key.getLocation().getNamespace()} and {@code key.getLocation().getPath()}.
	 *
	 * @param <E>        the type of elements in the dynamic registry
	 * @param key        a {@link RegistryKey#ofRegistry(Identifier) key for the new dynamic registry}
	 * @param entryCodec the codec used to deserialize entries from datapacks
	 * @param syncCodec  the codec used to (de)serialize entries to and from packets - may be the same as {@code entryCodec}
	 * @throws IllegalStateException if this registry of registries already got frozen
	 * @see #registerSynced(RegistryKey, Codec, DynamicRegistryFlag...)
	 */
	public static <E> void registerSynced(RegistryKey<? extends Registry<E>> key, Codec<E> entryCodec, Codec<E> syncCodec, DynamicRegistryFlag... flags) {
		DynamicMetaRegistryImpl.registerSynced(key, entryCodec, syncCodec, flags);
	}

	/**
	 * Registers a server-side dynamic registry.
	 * <p>
	 * Entries will be loaded from {@code "data/<namespace>/<registry_namespace>/<registry_path>"} for every datapack
	 * {@code namespace}, where {@code registry_namespace} and {@code registry_path}'s values are respectively
	 * {@code key.getLocation().getNamespace()} and {@code key.getLocation().getPath()}.
	 *
	 * @param <E>        the type of elements in the dynamic registry
	 * @param key        a {@link RegistryKey#ofRegistry(Identifier) key for the new dynamic registry}
	 * @param entryCodec the codec used to deserialize entries from datapacks
	 * @throws IllegalStateException if this registry of registries already got frozen
	 */
	public static <E> void register(RegistryKey<? extends Registry<E>> key, Codec<E> entryCodec) {
		DynamicMetaRegistryImpl.register(key, entryCodec);
	}

	/**
	 * Registers a dynamic registry which contents get synced between the server and connected clients.
	 * <p>
	 * Entries will be loaded from {@code "data/<namespace>/<registry_namespace>/<registry_path>"} for every datapack
	 * {@code namespace}, where {@code registry_namespace} and {@code registry_path}'s values are respectively
	 * {@code key.getLocation().getNamespace()} and {@code key.getLocation().getPath()}.
	 *
	 * @param <E>        the type of elements in the dynamic registry
	 * @param key        a {@link RegistryKey#ofRegistry(Identifier) key for the new dynamic registry}
	 * @param entryCodec the codec used to both deserialize entries from datapacks and (de)serialize entries to and from packets
	 * @throws IllegalStateException if this registry of registries already got frozen
	 * @see #registerSynced(RegistryKey, Codec, Codec)
	 */
	public static <E> void registerSynced(RegistryKey<? extends Registry<E>> key, Codec<E> entryCodec) {
		DynamicMetaRegistryImpl.registerSynced(key, entryCodec, entryCodec);
	}

	/**
	 * Registers a dynamic registry which contents get synced between the server and connected clients.
	 * <p>
	 * Entries will be loaded from {@code "data/<namespace>/<registry_namespace>/<registry_path>"} for every datapack
	 * {@code namespace}, where {@code registry_namespace} and {@code registry_path}'s values are respectively
	 * {@code key.getLocation().getNamespace()} and {@code key.getLocation().getPath()}.
	 *
	 * @param <E>        the type of elements in the dynamic registry
	 * @param key        a {@link RegistryKey#ofRegistry(Identifier) key for the new dynamic registry}
	 * @param entryCodec the codec used to deserialize entries from datapacks
	 * @param syncCodec  the codec used to (de)serialize entries to and from packets - may be the same as {@code entryCodec}
	 * @throws IllegalStateException if this registry of registries already got frozen
	 * @see #registerSynced(RegistryKey, Codec)
	 */
	public static <E> void registerSynced(RegistryKey<? extends Registry<E>> key, Codec<E> entryCodec, Codec<E> syncCodec) {
		DynamicMetaRegistryImpl.registerSynced(key, entryCodec, syncCodec);
	}
}
