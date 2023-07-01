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
public final class ClientPackets {
	/**
	 * Response for {@link ServerPackets#HANDSHAKE}. Selects the registry sync version to be used from the server's supported options.
	 *
	 * <pre><code>
	 * {
	 *     Supported Version: VarInt
	 * }
	 * </code></pre>
	 */
	public static final Identifier HANDSHAKE = id("registry_sync/handshake");

	/**
	 * Sent after registry sync failure before client disconnect.
	 *
	 * <pre><code>
	 * {
	 *     Registry: Identifier
	 * }
	 * </code></pre>
	 */
	public static final Identifier SYNC_FAILED = id("registry_sync/sync_failed");

	/**
	 * Sent after synchronization of selected registry.
	 * Contains list of (optional) unknown entries.
	 * It's sent after successful validation of {@link ServerPackets#REGISTRY_APPLY}
	 * Requires protocol version 3 or higher.
	 *
	 * <pre><code>
	 * {
	 *     Registry: Identifier
	 *     Raw Registry Ids: IntList
	 * }
	 * </code></pre>
	 */
	public static final Identifier UNKNOWN_ENTRY = id("registry_sync/unknown_entry");

	/**
	 * Sent after receiving Mod Protocol request packet from server.
	 * Returns all latest supported by client version of requested Mod Protocols see {@link ServerPackets#MOD_PROTOCOL}
	 *
	 * <pre><code>
	 * {
	 *   Count of Entries: VarInt
	 *   [
	 *     Id: String
	 *     Highest Supported Version: VarInt
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier MOD_PROTOCOL = id("registry_sync/mod_protocol");

	private static Identifier id(String path) {
		return new Identifier("qsl", path);
	}
}
