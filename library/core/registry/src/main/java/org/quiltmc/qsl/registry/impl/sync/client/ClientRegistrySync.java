/*
 * Copyright 2022-2023 QuiltMC
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registry;
import net.minecraft.util.collection.IdList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.impl.sync.ClientPackets;
import org.quiltmc.qsl.registry.impl.sync.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.ServerPackets;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedIdList;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;
import org.quiltmc.qsl.registry.mixin.client.ClientLoginNetworkHandlerAccessor;
import org.quiltmc.qsl.registry.mixin.client.ItemRendererAccessor;

@ApiStatus.Internal
@ClientOnly
public final class ClientRegistrySync {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Nullable
	private static Identifier currentRegistryId;

	@Nullable
	private static SynchronizedRegistry<?> currentRegistry;

	@Nullable
	private static Map<String, Collection<SynchronizedRegistry.SyncEntry>> syncMap;

	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private static int syncVersion = -1;
	@SuppressWarnings("unused")
	private static int currentCount;
	@SuppressWarnings("unused")
	private static byte currentFlags;
	private static boolean optionalRegistry;

	public static void registerHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.HANDSHAKE, ClientRegistrySync::handleHelloPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_START, ClientRegistrySync::handleStartPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_DATA, ClientRegistrySync::handleDataPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_APPLY, ClientRegistrySync::handleApplyPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.END, ClientRegistrySync::handleGoodbyePacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_RESTORE, ClientRegistrySync::handleRestorePacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.VALIDATE_BLOCK_STATES, handleStateValidation(Registries.BLOCK, Block.STATE_IDS, BlockState::isOf));
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.VALIDATE_FLUID_STATES, handleStateValidation(Registries.FLUID, Fluid.STATE_IDS, FluidState::isOf));
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

		// Capture the highest supported version for detecting what the server is sending.
		// This is required as older versions of registry sync erroneously sent RESTORE in place of APPLY.
		syncVersion = highestSupported;

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

		handler.sendPacket(ClientPlayNetworking.createC2SPacket(ClientPackets.SYNC_FAILED, buf));
	}

	private static void sendSyncUnknownEntriesPacket(ClientPlayNetworkHandler handler, Identifier identifier, IntList entries) {
		var buf = PacketByteBufs.create();
		buf.writeIdentifier(identifier);
		buf.writeIntList(entries);

		handler.sendPacket(ClientPlayNetworking.createC2SPacket(ClientPackets.UNKNOWN_ENTRY, buf));
	}

	private static void handleGoodbyePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		syncVersion = -1;
	}

	private static void handleStartPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		var identifier = buf.readIdentifier();
		int count = buf.readVarInt();
		byte flags = buf.readByte();

		var registry = Registries.REGISTRY.get(identifier);

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
			((ClientLoginNetworkHandlerAccessor) handler).getConnection().disconnect(getMessage("missing_registry", "Client is missing required registry! Mismatched mods?"));
		}
	}

	private static void handleDataPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		if (currentRegistry == null || syncMap == null) {
			if (optionalRegistry) {
				if (syncMap != null && !syncMap.isEmpty()) {
					var optionalMissing = new IntArrayList(Math.max(currentCount, 0));

					for (var entry : syncMap.values()) {
						for (var value : entry) {
							optionalMissing.add(value.rawId());
						}
					}

					sendSyncUnknownEntriesPacket(handler, currentRegistryId, optionalMissing);
				}
			} else {
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

	@SuppressWarnings("EqualsBetweenInconvertibleTypes")
	private static void handleApplyPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		if (currentRegistry == null || syncMap == null) {
			if (!optionalRegistry) {
				LOGGER.warn("Received sync data without specifying registry!");
			}

			return;
		}

		var reg = currentRegistry;
		var missingEntries = currentRegistry.quilt$applySyncMap(syncMap);

		if (!optionalRegistry && checkMissingAndDisconnect(handler, currentRegistryId, missingEntries)) {
			clear();
			return;
		}

		if (reg == Registries.BLOCK) {
			rebuildBlocks(client);
		} else if (reg == Registries.FLUID) {
			rebuildFluidStates();
		} else if (reg == Registries.ITEM) {
			rebuildItems(client);
		} else if (reg == Registries.PARTICLE_TYPE) {
			rebuildParticles(client);
		}

		if (syncVersion >= 3) {
			var optionalMissing = new IntArrayList();
			for (var entry : missingEntries) {
				if (RegistryFlag.isOptional(entry.flags())) {
					optionalMissing.add(entry.rawId());
				}
			}

			if (!optionalMissing.isEmpty()){
				sendSyncUnknownEntriesPacket(handler, currentRegistryId, optionalMissing);
			}
		}

		clear();
	}


	private static <T, S> ClientPlayNetworking.ChannelReceiver handleStateValidation(Registry<T> registry, IdList<S> stateList, BiPredicate<S, T> isOf) {
		return (MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) -> {
			var count = buf.readVarInt();

			while (count-- > 0) {
				var block = registry.get(buf.readVarInt());

				var stateCount = buf.readVarInt();

				while (stateCount-- > 0) {
					var state = stateList.get(buf.readVarInt());
					if (state == null || !isOf.test(state, block)) {
						disconnect(handler, getMessage("missing_entries", "Client is missing required states! Mismatched mods?"));
						LOGGER.warn("Failed to match state of " + registry.getId(block));
					}
				}
			}
		};
	}

	private static void clear() {
		currentRegistry = null;
		currentRegistryId = null;
		currentCount = 0;
		currentFlags = (byte) 0;
		optionalRegistry = false;

		syncMap = null;
	}

	private static void rebuildItems(MinecraftClient client) {
		var models = client.getItemRenderer().getModels();

		((RebuildableIdModelHolder) models).quilt$rebuildIds();
		models.reloadModels();

		var itemColors = ((ItemRendererAccessor) client.getItemRenderer()).getColors();
		((RebuildableIdModelHolder) itemColors).quilt$rebuildIds();
	}

	private static void rebuildParticles(MinecraftClient client) {
		((RebuildableIdModelHolder) client.particleManager).quilt$rebuildIds();
	}

	public static void rebuildBlocks(MinecraftClient client) {
		((RebuildableIdModelHolder) client.getBlockColors()).quilt$rebuildIds();

		SynchronizedIdList.clear(Block.STATE_IDS);

		for (var block : Registries.BLOCK) {
			block.getStateManager().getStates().forEach(Block.STATE_IDS::add);
		}
	}

	public static void rebuildFluidStates() {
		SynchronizedIdList.clear(Fluid.STATE_IDS);

		for (var fluid : Registries.FLUID) {
			fluid.getStateManager().getStates().forEach(Fluid.STATE_IDS::add);
		}
	}

	public static void rebuildEverything(MinecraftClient client) {
		rebuildBlocks(client);
		rebuildFluidStates();
		rebuildItems(client);
		rebuildParticles(client);
	}

	static Text getMessage(String type, String fallback) {
		if (Language.getInstance().hasTranslation("quilt.core.registry_sync." + type)) {
			return Text.translatable("quilt.core.registry_sync." + type);
		} else {
			return Text.literal(fallback);
		}
	}

	public static boolean checkMissingAndDisconnect(ClientPlayNetworkHandler handler, Identifier registry, Collection<SynchronizedRegistry.MissingEntry> missingEntries) {
		boolean disconnect = false;

		for (var entry : missingEntries) {
			if (!RegistryFlag.isOptional(entry.flags())) {
				disconnect = true;
				break;
			}
		}

		if (disconnect) {
			if (syncVersion != -1) {
				sendSyncFailedPacket(handler, registry);
			}

			disconnect(handler, getMessage("missing_entries", "Client registry is missing entries! Mismatched mods?"));
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

	private static void disconnect(ClientPlayNetworkHandler handler, Text reason) {
		handler.getConnection().disconnect(reason);
	}

	private static void handleRestorePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		restoreSnapshot(client);
		createSnapshot();
	}

	public static void createSnapshot() {
		for (var reg : Registries.REGISTRY) {
			if (reg instanceof SynchronizedRegistry registry && registry.quilt$requiresSyncing()) {
				registry.quilt$createIdSnapshot();
			}
		}
	}

	public static void restoreSnapshot(MinecraftClient client) {
		for (var reg : Registries.REGISTRY) {
			if (reg instanceof SynchronizedRegistry registry && registry.quilt$requiresSyncing()) {
				registry.quilt$restoreIdSnapshot();
			}
		}

		rebuildEverything(client);
	}
}
