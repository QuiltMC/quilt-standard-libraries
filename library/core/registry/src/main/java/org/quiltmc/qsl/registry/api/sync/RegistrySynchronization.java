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

package org.quiltmc.qsl.registry.api.sync;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;
import org.quiltmc.qsl.registry.impl.sync.server.ExtendedConnectionClient;

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

	/**
	 * Checks if player supports provided optional registry entry.
	 *
	 * @param player target player's entity
	 * @param registry registry entry is part of
	 * @param entry target entry
	 * @return {@code true} if the given entry is known to player, or {@code false} otherwise
	 */
	@Contract(pure = true)
	public static <T> boolean isEntryPresent(@NotNull ServerPlayerEntity player, @NotNull SimpleRegistry<T> registry, T entry) {
		return player.networkHandler != null && isEntryPresent(player.networkHandler, registry, entry);
	}

	/**
	 * Checks if player supports provided optional registry entry.
	 *
	 * @param handler target player's network handler
	 * @param registry registry entry is part of
	 * @param entry target entry
	 * @return {@code true} if the given entry is known to player, or {@code false} otherwise
	 */
	@Contract(pure = true)
	public static <T> boolean isEntryPresent(@NotNull ServerPlayNetworkHandler handler, @NotNull SimpleRegistry<T> registry, T entry) {
		var connection = ExtendedConnectionClient.from(handler);
		var regFlags = SynchronizedRegistry.as(registry).quilt$getRegistryFlag();
		var flags = SynchronizedRegistry.as(registry).quilt$getEntryFlag(entry);

		if (RegistryFlag.isSkipped(flags) || RegistryFlag.isSkipped(regFlags)) {
			return false;
		}

		if (connection.quilt$understandsOptional()) {
			return !connection.quilt$isUnknownEntry(registry, entry);
		} else {
			return !RegistryFlag.isOptional(flags) && !RegistryFlag.isOptional(regFlags);
		}
	}
}
