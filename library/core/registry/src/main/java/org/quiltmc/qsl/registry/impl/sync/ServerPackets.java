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
	public static final Identifier HELLO = id("registry/hello");

	/**
	 * Ends registry sync. No data
	 */
	public static final Identifier GOODBYE = id("registry/goodbye");

	/**
	 * Sets registry next data packets belong to
	 * <pre><code>
	 * {
	 *   Registry identifier: Identifier
	 *   Count: VarInt
	 *   Flags: byte
	 * }
	 * </code></pre>
	 */
	public static final Identifier START = id("registry/start");

	/**
	 * This packet transmits registry data required for sync
	 * <pre><code>
	 * {
	 *   Count: VarInt
	 *   [
	 *     Common Namespace: String
	 *     Count: VarInt
	 *     [
	 *       Path: String
	 *       Id: VarInt
	 *       Flags: byte
	 *     ]
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier DATA = id("registry/data");

	/**
	 * Applies changes, doesn't have any data
	 */
	public static final Identifier APPLY = id("registry/apply");


	private static Identifier id(String path) {
		return new Identifier("qsl", path);
	}
}
