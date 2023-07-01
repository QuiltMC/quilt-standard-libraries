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

package org.quiltmc.qsl.registry.impl.sync;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

/**
 * Identifiers of packets sent by server.
 */
@ApiStatus.Internal
public final class ServerPackets {
	/**
	 * Starts registry sync.
	 *
	 * <pre><code>
	 * {
	 *   Supported Versions: IntList
	 * }
	 * </code></pre>
	 */
	public static final Identifier HANDSHAKE = id("registry_sync/handshake");

	/**
	 * Ends registry sync. No data
	 */
	public static final Identifier END = id("registry_sync/end");

	/**
	 * Sets current registry for next {@link ServerPackets#REGISTRY_DATA} and {@link ServerPackets#REGISTRY_RESTORE} packets.
	 *
	 * <pre><code>
	 * {
	 *   Registry identifier: Identifier
	 *   Count of entries: VarInt
	 *   Flags: byte
	 * }
	 * </code></pre>
	 */
	public static final Identifier REGISTRY_START = id("registry_sync/registry_start");

	/**
	 * This packet transmits registry data required for sync.
	 *
	 * <pre><code>
	 * {
	 *   Count of Namespaces: VarInt
	 *   [
	 *     Common Namespace: String
	 *     Count of Entries: VarInt
	 *     [
	 *       Path: String
	 *       Id: VarInt
	 *       Flags: byte
	 *     ]
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier REGISTRY_DATA = id("registry_sync/registry_data");

	/**
	 * Applies changes to current registry, doesn't have any data.
	 */
	public static final Identifier REGISTRY_APPLY = id("registry_sync/registry_apply");

	/**
	 * This packet requests client to validate ids of {@link net.minecraft.block.Block#STATE_IDS} to prevent id mismatch.
	 * It might not send all states in single packet! It needs to be verified as is (aka, ids matter, count doesn't).
	 *
	 * <pre><code>
	 * {
	 *   Count of Entries: VarInt
	 *   [
	 *     Block Id: VarInt
	 *     Count of Entries: VarInt
	 *     [
	 *       BlockState Id: VarInt
	 *     ]
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier VALIDATE_BLOCK_STATES = id("registry_sync/validate/block_states");
	/**
	 * Same structure as {@link ServerPackets#VALIDATE_BLOCK_STATES}, but for FluidStates
	 */
	public static final Identifier VALIDATE_FLUID_STATES = id("registry_sync/validate/fluid_states");

	/**
	 * Applies changes to current registry, doesn't have any data.
	 */
	public static final Identifier REGISTRY_RESTORE = id("registry_sync/registry_restore");

	/**
	 * This packet sets failure text look/properties.
	 * Requires protocol version 3 or newer.
	 *
	 * <pre><code>
	 * {
	 *   Text Header: Text (String)
	 *   Text Footer: Text (String)
	 *   Show Details: bool
	 *
	 * }
	 * </code></pre>
	 */
	public static final Identifier ERROR_STYLE = id("registry_sync/error_style");

	/**
	 * This packet requests client to validate and return supported Mod Protocol versions.
	 *
	 * <pre><code>
	 * {
	 *   Prioritized Id: String
	 *   Count of Entries: VarInt
	 *   [
	 *     Id: String
	 *     Name: String
	 *     Supported Versions: IntList
	 *     Optional: boolean
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier MOD_PROTOCOL = id("registry_sync/mod_protocol");

	private static Identifier id(String path) {
		return new Identifier("qsl", path);
	}
}
