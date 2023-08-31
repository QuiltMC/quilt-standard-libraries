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

package org.quiltmc.qsl.registry.api.sync;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;
import org.quiltmc.qsl.registry.impl.sync.server.ExtendedConnectionClient;

/**
 * Methods for getting supported bt client mod protocol versions
 */
@ApiStatus.Experimental
public final class ModProtocol {
	public static final int UNSUPPORTED = -1;
	private ModProtocol() {}

	/**
	 * {@return {@code true} if Self Mod Protocol is enabled, otherwise {@code false}}
	 */
	@Contract(pure = true)
	public static boolean isSelfEnabled() {
		return ModProtocolImpl.prioritizedEntry != null;
	}

	/**
	 * {@return a {@code String} representing Self Mod Protocol id, otherwise {@code null}}
	 */
	@Contract(pure = true)
	@Nullable
	public static String getSelfId() {
		return isSelfEnabled() ? ModProtocolImpl.prioritizedEntry.id() : null;
	}

	/**
	 * {@return a {@code String} representing Self Mod Protocol display name, otherwise {@code null}}
	 */
	@Contract(pure = true)
	@Nullable
	public static String getSelfDisplayName() {
		return isSelfEnabled() ? ModProtocolImpl.prioritizedEntry.displayName() : null;
	}

	/**
	 * Checks the mod protocol version supported by a given player.
	 *
	 * @param player Player to check against
	 * @param modContainer ModContainer defining protocol version
	 * @return latest supported by player protocol for mod. -1 if not supported
	 */
	@Contract(pure = true)
	public static int getSupported(@NotNull ServerPlayerEntity player, @NotNull ModContainer modContainer) {
		return player.networkHandler != null ? getSupported(player.networkHandler, modContainer) : UNSUPPORTED;
	}

	/**
	 * Checks the mod protocol version supported by a given player.
	 *
	 * @param handler player's network handler to check against
	 * @param modContainer ModContainer defining protocol version
	 * @return latest supported by player protocol for mod. -1 if not supported
	 */
	@Contract(pure = true)
	public static int getSupported(@NotNull ServerPlayNetworkHandler handler, @NotNull ModContainer modContainer) {
		return ExtendedConnectionClient.from(handler).quilt$getModProtocol("mod:" + modContainer.metadata().id());
	}
}
