package org.quiltmc.qsl.registry.impl.sync;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ServerPackets {
	public static final Identifier HELLO = id("registry/hello");

	/**
	 * Sets registry next data packets belong to
	 * {
	 *   Registry identifier: Identifier
	 *   Count: VarInt
	 *   Flags: byte
	 * }
	 */
	public static final Identifier START = id("registry/start");

	/**
	 * This packet transmits required for sync
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
