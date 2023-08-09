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
 * Utilities for getting information about active Mod Protocols, a system for requiring compatible versions
 * of a mod or modpack to be installed on the client when connecting to a server.
 * <p>
 * <h1>The Mod Protocol System</h1>
 * <h2>Overview</h2>
 * The mod protocol system allows mods and modpacks to require specific version ranges (represented as a protocol integer)
 * to be present on both sides when joining a server.
 * <p>
 * When a client pings a server or attempts to connect, the server sends to the client a list of ALL mod protocols, containing
 * each one's id, display name and a list of supported protocol versions. Upon connection, the client replies with a list
 * of the highest version that is supported by the client and server for each protocol. The negotiated protocol version can
 * be queried with {@link ModProtocols#getSupported(ServerPlayerEntity, ModContainer)}.
 * <p>
 * If a client does not support any of the protocol versions requested by the server, then the client will not be allowed
 * to connect (unless the mod protocol is marked optional).
 * <h2>Configuration</h2>
 * Mods can define a mod protocol in their {@code quilt.mod.json}.
 * <pre> {@code
 * {
 *     "quilt_loader": {...},
 *     "quilt_registry": {
 *         // May be a single number or an array of supported versions.
 *         "mod_protocol": [1, 2, 4]
 *     }
 * }
 * }</pre>
 *
 *  The id of the protocol is identical to the mod id. The display name is always the mod's display name (or ID if none exists)
 *  followed by the version, e.g. "Quilt Registry API v4.1.0"
 * Protocols can also be marked optional, which means they are not required to be supported by both sides in order
 * to connect:
 * <pre> {@code
 * {
 *     "quilt_loader": {...},
 *     "quilt_registry": {
 *         "mod_protocol": {
 *             "value": 1,
 *             "optional": true
 *         }
 *     }
 * }
 * }</pre>
 * <p>
 * In addition to mods defining their own mod protocols, users can define a special modpack protocol in the Quilt Registry
 * config (located at {@code configs/quilt/qsl/registry.toml}). Other than being defined by the user, the modpack protocol
 * is otherwise idential to standard mod protocols.
 */
@ApiStatus.Experimental
public final class ModProtocols {
	public static final int UNSUPPORTED = -1;
	private ModProtocols() {}

	/**
	 * {@return {@code true} if a modpack protocol has been provided through the config, otherwise {@code false}}
	 */
	@Contract(pure = true)
	public static boolean isModpackProtocolEnabled() {
		return ModProtocolImpl.modpackDef != null;
	}

	/**
	 * {@return a {@code String} representing the modpack protocol id, otherwise {@code null}}
	 */
	@Contract(pure = true)
	@Nullable
	public static String getModpackProtocolId() {
		return isModpackProtocolEnabled() ? ModProtocolImpl.modpackDef.id() : null;
	}

	/**
	 * {@return a {@code String} representing the modpack protocol display name, otherwise {@code null}}
	 */
	@Contract(pure = true)
	@Nullable
	public static String getModpackDisplayName() {
		return isModpackProtocolEnabled() ? ModProtocolImpl.modpackDef.displayName() : null;
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

	/**
	 * Checks the modpack protocol version supported by a given player. Returns -1 if the server and player's
	 * {@linkplain #getModpackProtocolId() modpack protocol id} do not match.
	 * @param handler player's network handler to check against
	 * @return latest supported by player protocol for this server's {@linkplain #getModpackProtocolId() modpack protocol id}. -1 if not supported
	 */
	@Contract(pure = true)
	public static int getSupportedModpack(@NotNull ServerPlayerEntity handler) {
		return getSupportedModpack(handler.networkHandler);
	}

	/**
	 * Checks the modpack protocol version supported by a given player. Returns -1 if the server and player's
	 * {@linkplain #getModpackProtocolId() modpack protocol id} do not match.
	 * @param handler player's network handler to check against
	 * @return latest supported by player protocol for this server's {@linkplain #getModpackProtocolId() modpack protocol id}. -1 if not supported
	 */
	@Contract(pure = true)
	public static int getSupportedModpack(@NotNull ServerPlayNetworkHandler handler) {
		return isModpackProtocolEnabled() ? ExtendedConnectionClient.from(handler).quilt$getModProtocol("modpack:" + getModpackProtocolId()) : UNSUPPORTED;
	}
}
