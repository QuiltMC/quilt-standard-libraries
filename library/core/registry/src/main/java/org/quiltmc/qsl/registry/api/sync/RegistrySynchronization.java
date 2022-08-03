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

package org.quiltmc.qsl.registry.api.sync;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.qsl.registry.impl.sync.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;

/**
 * Methods for manipulation of registry synchronization.
 */
@ApiStatus.Experimental
public final class RegistrySynchronization {

	/**
	 * Marks registry status as dirty, requiring its synchronization data to be rebuilt.
	 */
	public static void markDirty(@NotNull SimpleRegistry<?> registry) {
		SynchronizedRegistry.as(registry).quilt$markDirty();
	}

	/**
	 * Makes registry synchronize with clients.
	 */
	public static void markForSync(@NotNull SimpleRegistry<?> registry) {
		SynchronizedRegistry.as(registry).quilt$markForSync();
	}

	/**
	 * Marks a registry as optional for synchronization.
	 */
	public static void setRegistryOptional(@NotNull SimpleRegistry<?> registry) {
		RegistryFlag.setRegistry(registry, RegistryFlag.OPTIONAL);
	}

	/**
	 * {@return {@code true} if the given registry is marked as optional, or {@code false} otherwise}
	 */
	@Contract(pure = true)
	public static boolean isRegistryOptional(@NotNull SimpleRegistry<?> registry) {
		return RegistryFlag.isOptional(SynchronizedRegistry.as(registry).quilt$getRegistryFlag());
	}

	/**
	 * Marks the given registry entry as optional of synchronization.
	 */
	public static <T> void setEntryOptional(@NotNull SimpleRegistry<T> registry, T entry) {
		RegistryFlag.setEntry(registry, entry, RegistryFlag.OPTIONAL);
	}

	/**
	 * Marks the specified registry entry as optional of synchronization.
	 */
	public static <T> void setEntryOptional(@NotNull SimpleRegistry<T> registry, @NotNull Identifier identifier) {
		RegistryFlag.setEntry(registry, identifier, RegistryFlag.OPTIONAL);
	}

	/**
	 * {@return {@code true} if the registry entry is marked as optional, or {@code false} otherwise}
	 */
	@Contract(pure = true)
	public static <T> boolean isEntryOptional(@NotNull SimpleRegistry<T> registry, T entry) {
		return RegistryFlag.isOptional(SynchronizedRegistry.as(registry).quilt$getEntryFlag(entry));
	}

	/**
	 * Prevents the given registry entry from being synced.
	 */
	public static <T> void setEntrySkipped(@NotNull SimpleRegistry<T> registry, T entry) {
		RegistryFlag.setEntry(registry, entry, RegistryFlag.SKIP);
	}

	/**
	 * Prevents the specified registry entry from being synced.
	 */
	public static <T> void setEntrySkipped(@NotNull SimpleRegistry<T> registry, @NotNull Identifier identifier) {
		RegistryFlag.setEntry(registry, identifier, RegistryFlag.SKIP);
	}

	/**
	 * {@return {@code true} if the given registry entry is skipped in synchronization, or {@code false} otherwise}
	 */
	@Contract(pure = true)
	public static <T> boolean isEntrySkipped(@NotNull SimpleRegistry<T> registry, T entry) {
		return RegistryFlag.isSkipped(SynchronizedRegistry.as(registry).quilt$getEntryFlag(entry));
	}
}
