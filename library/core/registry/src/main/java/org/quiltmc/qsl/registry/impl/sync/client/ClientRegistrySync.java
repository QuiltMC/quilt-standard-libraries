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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
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

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
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
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.HANDSHAKE, ClientRegistrySync::handleHelloPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_START, ClientRegistrySync::handleStartPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_DATA, ClientRegistrySync::handleDataPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_RESTORE, ClientRegistrySync::handleApplyPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.END, ClientRegistrySync::handleGoodbyePacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_RESTORE, ClientRegistrySync::handleRestorePacket);
	}

	private static void handleHelloPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		int count = buf.readVarInt();

		int highestSupported = -1;

		while (count-- > 0) {
			int version = buf.readVarInt();

			if (version > highestSupported && ServerPackets.SUPPORTED_VERSIONS.contains(version)) {
				highestSupported = version;
			}
		}

		sendHelloPacket(sender, highestSupported);
	}

	private static void sendHelloPacket(PacketSender sender, int version) {
		var buf = PacketByteBufs.create();
		buf.writeVarInt(version);

		sender.sendPacket(ClientPackets.HANDSHAKE, buf);
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
		int count = buf.readVarInt();
		byte flags = buf.readByte();

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
			LOGGER.warn("Trying to sync registry " + identifier + " which doesn't " + (registry != null ? "support it!" : "exist!"));
			sendSyncFailedPacket(handler, identifier);
			handler.getConnection().disconnect(getMessage("missing_registry", "Client is missing required registry! Mismatched mods?"));
		}
	}

	private static void handleDataPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		if (currentRegistry == null || syncMap == null) {
			if (!optionalRegistry) {
				LOGGER.warn("Received sync data without specifying registry!");
			}
			return;
		}

		int countNamespace = buf.readVarInt();
		while (countNamespace-- > 0) {
			var namespace = buf.readString();
			int countLocal = buf.readVarInt();

			while (countLocal-- > 0) {
				var path = buf.readString();
				int id = buf.readVarInt();
				byte flags = buf.readByte();

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
			disconnect = checkMissing(handler, currentRegistryId, missingEntries);
		}


		if (!disconnect && reg == Registry.BLOCK) {
			rebuildBlockStates();
		} else if (!disconnect && reg == Registry.FLUID) {
			rebuildFluidStates();
		}

		currentRegistry = null;
		currentRegistryId = null;
		currentCount = 0;
		currentFlags = (byte) 0;
		optionalRegistry = false;

		syncMap = null;
	}

	public static void rebuildBlockStates() {
		SynchronizedIdList.clear(Block.STATE_IDS);

		for (var block : Registry.BLOCK) {
			block.getStateManager().getStates().forEach(Block.STATE_IDS::add);
		}
	}

	public static void rebuildFluidStates() {
		SynchronizedIdList.clear(Fluid.STATE_IDS);

		for (var fluid : Registry.FLUID) {
			fluid.getStateManager().getStates().forEach(Fluid.STATE_IDS::add);
		}
	}

	static Text getMessage(String type, String fallback) {
		if (Language.getInstance().hasTranslation("quilt.core.registry_sync." + type)) {
			return new TranslatableText("quilt.core.registry_sync." + type);
		} else {
			return new LiteralText(fallback);
		}
	}

	public static boolean checkMissing(ClientPlayNetworkHandler handler, Identifier registry, Collection<SynchronizedRegistry.MissingEntry> missingEntries) {
		boolean disconnect = false;

		for (var entry : missingEntries) {
			if (!RegistryFlag.isOptional(entry.flags())) {
				disconnect = true;
				break;
			}
		}

		if (disconnect) {
			sendSyncFailedPacket(handler, registry);
			handler.getConnection().disconnect(getMessage("missing_entries", "Client registry is missing entries! Mismatched mods?"));
			var builder = new StringBuilder("Missing entries for registry \"" + registry + "\":\n");

			for (var entry : missingEntries) {
				builder.append("\t- ").append(entry.identifier());
				if (RegistryFlag.isOptional(entry.flags())) {
					builder.append(" (Optional)");
				}
				builder.append("\n");
			}

			LOGGER.warn(builder.toString());
		}

		return disconnect;
	}

	private static void handleRestorePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		restoreSnapshot();
		createSnapshot();
	}

	public static void createSnapshot() {
		for (var reg : Registry.REGISTRIES) {
			if (reg instanceof SynchronizedRegistry registry && registry.quilt$requiresSyncing()) {
				registry.quilt$createIdSnapshot();
			}
		}
	}

	public static void restoreSnapshot() {
		for (var reg : Registry.REGISTRIES) {
			if (reg instanceof SynchronizedRegistry registry && registry.quilt$requiresSyncing()) {
				registry.quilt$restoreIdSnapshot();
			}
		}
		rebuildBlockStates();
		rebuildFluidStates();
	}
}
