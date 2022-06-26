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

package org.quiltmc.qsl.registry.impl.sync;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Identifiers of packets sent by server
 */
@ApiStatus.Internal
public final class ServerPackets {
	public static final IntSet SUPPORTED_VERSIONS = IntSet.of(1);
	/**
	 * Starts registry sync
	 * <pre><code>
	 * {
	 *   Count: VarInt
	 *   [
	 *     Supported Version: VarInt
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier HANDSHAKE = id("registry_sync/handshake");

	/**
	 * Ends registry sync. No data
	 */
	public static final Identifier END = id("registry_sync/end");

	/**
	 * Sets current registry for next {@link ServerPackets#REGISTRY_DATA} and {@link ServerPackets#REGISTRY_RESTORE} packets
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
	 * This packet transmits registry data required for sync
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
	 * Applies changes to current registry, doesn't have any data.
	 */
	public static final Identifier REGISTRY_RESTORE = id("registry_sync/registry_restore");


	private static Identifier id(String path) {
		return new Identifier("qsl", path);
	}
}
