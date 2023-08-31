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

package org.quiltmc.qsl.registry.impl.sync.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.text.component.LiteralComponent;
import net.minecraft.text.component.TextComponent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.impl.sync.ClientPackets;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;
import org.quiltmc.qsl.registry.impl.sync.RegistrySyncText;
import org.quiltmc.qsl.registry.impl.sync.ServerPackets;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolDef;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;
import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedIdList;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;
import org.quiltmc.qsl.registry.impl.sync.server.ServerRegistrySync;
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

	private static Text errorStyleHeader = ServerRegistrySync.errorStyleHeader;
	private static Text errorStyleFooter = ServerRegistrySync.errorStyleFooter;
	private static boolean showErrorDetails = ServerRegistrySync.showErrorDetails;

	private static Text disconnectMainReason = null;

	private static LogBuilder builder = new LogBuilder();
	private static boolean mustDisconnect;

	@Nullable
	public static List<LogBuilder.Section> getAndClearCurrentSyncLogs() {
		return builder.finish();
	}

	public static void registerHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.HANDSHAKE, ClientRegistrySync::handleHelloPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_START, ClientRegistrySync::handleStartPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_DATA, ClientRegistrySync::handleDataPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_APPLY, ClientRegistrySync::handleApplyPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.END, ClientRegistrySync::handleGoodbyePacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.REGISTRY_RESTORE, ClientRegistrySync::handleRestorePacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.VALIDATE_BLOCK_STATES, handleStateValidation(Registries.BLOCK, Block.STATE_IDS, BlockState::getBlock));
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.VALIDATE_FLUID_STATES, handleStateValidation(Registries.FLUID, Fluid.STATE_IDS, FluidState::getFluid));
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.ERROR_STYLE, ClientRegistrySync::handleErrorStylePacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.MOD_PROTOCOL, ClientRegistrySync::handleModProtocol);
	}

	private static void handleModProtocol(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		var prioritizedId = buf.readString();
		var protocols = buf.readList(ModProtocolDef::read);

		var values = new Object2IntOpenHashMap<String>(protocols.size());
		var unsupportedList = new ArrayList<ModProtocolDef>();
		ModProtocolDef missingPrioritized = null;

		boolean disconnect = false;

		for (var protocol : protocols) {
			var local = ModProtocolImpl.getVersion(protocol.id());
			var latest = protocol.latestMatchingVersion(local);
			System.out.println(latest);
			if (latest != ProtocolVersions.NO_PROTOCOL) {
				values.put(protocol.id(), latest);
			} else if (!protocol.optional()) {
				unsupportedList.add(protocol);
				disconnect = true;
				if (prioritizedId.equals(protocol.id())) {
					missingPrioritized = protocol;
				}
			}
		}

		if (disconnect) {
			markDisconnected(handler, RegistrySyncText.unsupportedModVersion(unsupportedList, missingPrioritized));

			builder.pushT("unsupported_protocol", "Unsupported Mod Protocol");

			for (var entry : unsupportedList) {
				builder.textEntry(Text.literal(entry.displayName()).append(Text.literal(" (" + entry.id() + ")").formatted(Formatting.DARK_GRAY)).append(" | Server: ").append(stringifyVersions(entry.versions())).append(", Client: ").append(stringifyVersions(ModProtocolImpl.getVersion(entry.id()))));
			}
		} else {
			sendSupportedModProtocol(sender, values);
		}
	}

	private static String stringifyVersions(IntList versions) {
		if (versions == null || versions.isEmpty()) {
			return "Missing!";
		}

		var b = new StringBuilder().append('[');

		var iter = versions.iterator();

		while (iter.hasNext()) {
			b.append(iter.nextInt());

			if (iter.hasNext()) {
				b.append(", ");
			}
		}

		return b.append(']').toString();
	}

	private static void sendSupportedModProtocol(PacketSender sender, Object2IntOpenHashMap<String> values) {
		var buf = PacketByteBufs.create();
		buf.writeVarInt(values.size());
		for (var entry : values.object2IntEntrySet()) {
			buf.writeString(entry.getKey());
			buf.writeVarInt(entry.getIntValue());
		}

		sender.sendPacket(ClientPackets.MOD_PROTOCOL, buf);
	}

	private static void handleErrorStylePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		errorStyleHeader = buf.readText();
		errorStyleFooter = buf.readText();
		showErrorDetails = buf.readBoolean();
	}

	private static void handleHelloPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		syncVersion = ProtocolVersions.getHighestSupportedLocal(buf.readIntList());
		sendHelloPacket(sender, syncVersion);
		builder.clear();
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

		if (mustDisconnect) {
			var entry = Text.empty();
			entry.append(errorStyleHeader);

			if (disconnectMainReason != null && showErrorDetails && !isTextEmpty(disconnectMainReason)) {
				entry.append("\n");
				entry.append(disconnectMainReason);
			}

			if (!isTextEmpty(errorStyleFooter)) {
				entry.append("\n");
				entry.append(errorStyleFooter);
			}

			handler.getConnection().disconnect(entry);

			LOGGER.warn(builder.asString());
		}
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
			sendSyncFailedPacket(handler, identifier);
			var x = RegistrySyncText.missingRegistry(identifier, registry != null);
			markDisconnected(handler, x);
			builder.push(x);
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
			clearState();
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

		var optionalMissing = new IntArrayList();
		for (var entry : missingEntries) {
			if (RegistryFlag.isOptional(entry.flags())) {
				optionalMissing.add(entry.rawId());
			}
		}

		if (!optionalMissing.isEmpty()) {
			sendSyncUnknownEntriesPacket(handler, currentRegistryId, optionalMissing);
		}

		clearState();
	}

	private static <T, S> ClientPlayNetworking.ChannelReceiver handleStateValidation(Registry<T> registry, IdList<S> stateList, Function<S, T> converter) {
		return (MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) -> {
			var count = buf.readVarInt();

			boolean firstMismatch = true;

			while (count-- > 0) {
				var block = registry.get(buf.readVarInt());

				var stateCount = buf.readVarInt();

				while (stateCount-- > 0) {
					var state = stateList.get(buf.readVarInt());
					if (state == null || converter.apply(state) != block) {
						var conv = state == null ? null : converter.apply(state);
						markDisconnected(handler, RegistrySyncText.mismatchedStateIds(registry.getKey().getValue(), block == null ? null : registry.getId(block), conv == null ? null : registry.getId(conv)));
						if (firstMismatch) {
							builder.pushT("state_mismatch", "Mismatched states for entries of '%s'", registry.getKey().getValue().toString());
							firstMismatch = false;
						}

						builder.textEntry(Text.translatableWithFallback("quilt.core.registry_sync.found_expected", "Found '%s', expected '%s'",
								block == null ? Text.literal("null").formatted(Formatting.RED) : registry.getId(block),
								conv == null ? Text.literal("null").formatted(Formatting.RED) : registry.getId(conv)));
					}
				}
			}
		};
	}

	private static void clearState() {
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

			markDisconnected(handler, RegistrySyncText.missingRegistryEntries(registry, missingEntries));
			builder.pushT("missing_entries", "Missing entries for registry '%s'", registry);

			for (var entry : missingEntries) {
				var x = Text.literal(entry.identifier().toString());
				if (RegistryFlag.isOptional(entry.flags())) {
					x.append(" ").append(Text.translatableWithFallback("quilt.core.registry_sync.optional", "(Optional)").formatted(Formatting.DARK_GRAY));
				}

				builder.textEntry(x);
			}
		}

		return disconnect;
	}

	private static void markDisconnected(ClientPlayNetworkHandler handler, Text reason) {
		if (disconnectMainReason == null) {
			disconnectMainReason = reason;
		}

		mustDisconnect = true;
	}

	private static boolean isTextEmpty(Text text) {
		return (text.asComponent() == TextComponent.EMPTY || (text.asComponent() instanceof LiteralComponent literalComponent && literalComponent.literal().isEmpty())) && text.getSiblings().isEmpty();
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

	public static void disconnectCleanup(MinecraftClient client) {
		clearState();
		restoreSnapshot(client);
		errorStyleHeader = ServerRegistrySync.errorStyleHeader;
		errorStyleFooter = ServerRegistrySync.errorStyleFooter;
		showErrorDetails = ServerRegistrySync.showErrorDetails;
		disconnectMainReason = null;
	}
}
