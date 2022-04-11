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

package org.quiltmc.qsl.registry.impl.sync.client;

import com.mojang.logging.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.impl.sync.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ClientRegistrySync {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Nullable
	private static Identifier currentRegistryId;

	@Nullable
	private static SynchronizedRegistry<?> currentRegistry;

	@Nullable
	private static Map<String, Collection<SynchronizedRegistry.SyncEntry>> syncMap;

	private static int currentCount;
	private static byte currentFlags;
	private static boolean optionalRegistry;

	public static void registerHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.HELLO, ClientRegistrySync::handleHelloPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.START, ClientRegistrySync::handleStartPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.DATA, ClientRegistrySync::handleDataPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.APPLY, ClientRegistrySync::handleApplyPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.GOODBYE, ClientRegistrySync::handleGoodbyePacket);

	}

	private static void handleHelloPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		var count = buf.readVarInt();

		int highestSupported = -1;

		while (count-- > 0) {
			var version = buf.readVarInt();

			if (version > highestSupported) {
				highestSupported = version;
			}
		}

		sendHelloPacket(sender, highestSupported);
	}

	private static void sendHelloPacket(PacketSender sender, int version) {
		var buf = PacketByteBufs.create();
		buf.writeVarInt(version);

		sender.sendPacket(ClientPackets.HELLO, buf);
	}

	public static void sendSyncFailedPacket(ClientPlayNetworkHandler handler, Identifier identifier) {
		var buf = PacketByteBufs.create();
		buf.writeIdentifier(identifier);

		handler.getConnection().send(ClientPlayNetworking.createC2SPacket(ClientPackets.SYNC_FAILED, buf));
	}

	private static void handleGoodbyePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
	}

	private static void handleStartPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		var identifier = buf.readIdentifier();
		var count = buf.readVarInt();
		var flags = buf.readByte();

		var registry = Registry.REGISTRIES.get(identifier);

		if (registry instanceof SynchronizedRegistry synchronizedRegistry) {
			currentRegistry = synchronizedRegistry;
			currentCount = count;
			currentFlags = flags;
			currentRegistryId = identifier;
			syncMap = new HashMap<>();
		} else if (RegistryFlag.isOptional(flags)) {
			optionalRegistry = true;
		} else {
			LOGGER.warn("Trying to sync registry " + identifier + " which doesn't " + (registry == null ? "support it!" : "exist!"));
			sendSyncFailedPacket(handler, identifier);
			handler.getConnection().disconnect(new LiteralText("Client is missing required registry! Mismatched mods?"));
		}
	}

	private static void handleDataPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		if (currentRegistry == null || syncMap == null) {
			if (!optionalRegistry) {
				LOGGER.warn("Received sync data without specifying registry!");
			}
			return;
		}

		var countNamespace = buf.readVarInt();
		while (countNamespace-- > 0) {
			var namespace = buf.readString();
			var countLocal = buf.readVarInt();

			while (countLocal-- > 0) {
				var path = buf.readString();
				var id = buf.readVarInt();
				var flags = buf.readByte();

				syncMap.computeIfAbsent(namespace, n -> new ArrayList<>()).add(new SynchronizedRegistry.SyncEntry(path, id, flags));
			}
		}
	}

	private static void handleApplyPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		if (currentRegistry == null || syncMap == null) {
			if (!optionalRegistry) {
				LOGGER.warn("Received sync data without specifying registry!");
			}
			return;
		}

		var reg = currentRegistry;
		var missingEntries = currentRegistry.quilt$applySyncMap(syncMap);

		boolean disconnect = false;

		if (!optionalRegistry) {
			for (var entry : missingEntries) {
				if (!RegistryFlag.isOptional(entry.flags())) {
					sendSyncFailedPacket(handler, currentRegistryId);
					handler.getConnection().disconnect(new LiteralText("Client registry is missing entries! Mismatched mods?"));
					break;
				}
			}
		}


		if (!disconnect && reg == Registry.BLOCK) {
			rebuildStates();
		}

		currentRegistry = null;
		currentRegistryId = null;
		currentCount = 0;
		currentFlags = (byte) 0;
		optionalRegistry = false;

		syncMap = null;
	}

	public static void rebuildStates() {
		SynchronizedIdList.clear(Block.STATE_IDS);

		for (var block : Registry.BLOCK) {
			block.getStateManager().getStates().forEach(Block.STATE_IDS::add);
		}
	}
}
