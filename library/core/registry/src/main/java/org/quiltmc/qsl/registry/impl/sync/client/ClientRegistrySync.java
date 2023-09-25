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
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.text.component.LiteralComponent;
import net.minecraft.text.component.TextComponent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.mixin.accessor.AbstractClientNetworkHandlerAccessor;
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
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.Handshake.ID, ClientRegistrySync::handleHelloPacket);
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.RegistryStart.ID, ClientRegistrySync::handleStartPacket);
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.RegistryData.ID, ClientRegistrySync::handleDataPacket);
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.RegistryApply.ID, ClientRegistrySync::handleApplyPacket);
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.End.ID, ClientRegistrySync::handleGoodbyePacket);
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.RegistryRestore.ID, ClientRegistrySync::handleRestorePacket);
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.ValidateStates.StateType.BLOCK.packetId(), handleStateValidation(Registries.BLOCK, Block.STATE_IDS, BlockState::getBlock));
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.ValidateStates.StateType.FLUID.packetId(), handleStateValidation(Registries.FLUID, Fluid.STATE_IDS, FluidState::getFluid));
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.ErrorStyle.ID, ClientRegistrySync::handleErrorStylePacket);
		ClientConfigurationNetworking.registerGlobalReceiver(ServerPackets.ModProtocol.ID, ClientRegistrySync::handleModProtocol);
	}

	private static void handleModProtocol(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.ModProtocol modProtocol, PacketSender<CustomPayload> sender) {
		var values = new Object2IntOpenHashMap<String>(modProtocol.protocols().size());
		var unsupportedList = new ArrayList<ModProtocolDef>();
		ModProtocolDef missingPrioritized = null;

		boolean disconnect = false;

		for (var protocol : modProtocol.protocols()) {
			var local = ModProtocolImpl.getVersion(protocol.id());
			var latest = protocol.latestMatchingVersion(local);
			System.out.println(latest);
			if (latest != ProtocolVersions.NO_PROTOCOL) {
				values.put(protocol.id(), latest);
			} else if (!protocol.optional()) {
				unsupportedList.add(protocol);
				disconnect = true;
				if (modProtocol.prioritizedId().equals(protocol.id())) {
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

	private static void sendSupportedModProtocol(PacketSender<CustomPayload> sender, Object2IntOpenHashMap<String> values) {
		sender.sendPayload(new ClientPackets.ModProtocol(values));
	}

	private static void handleErrorStylePacket(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.ErrorStyle style, PacketSender<CustomPayload> sender) {
		errorStyleHeader = style.errorHeader();
		errorStyleFooter = style.errorFooter();
		showErrorDetails = style.showError();
	}

	private static void handleHelloPacket(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.Handshake handshake, PacketSender<CustomPayload> sender) {
		syncVersion = ProtocolVersions.getHighestSupportedLocal(handshake.supportedVersions());
		sender.sendPayload(new ClientPackets.Handshake(syncVersion));
		builder.clear();
	}

	private static void handleGoodbyePacket(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.End end, PacketSender<CustomPayload> sender) {
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

			((AbstractClientNetworkHandlerAccessor) handler).getConnection().disconnect(entry);

			LOGGER.warn(builder.asString());
		} else {
			sender.sendPayload(new ClientPackets.End());
		}
	}

	private static void handleStartPacket(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.RegistryStart<?> start, PacketSender<CustomPayload> sender) {
		var registry = Registries.REGISTRY.get(start.registry());
		if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry) {
			currentRegistry = synchronizedRegistry;
			currentCount = start.size();
			currentFlags = start.flags();
			currentRegistryId = start.registry();
			syncMap = new HashMap<>();
		} else if (RegistryFlag.isOptional(start.flags())) {
			optionalRegistry = true;
		} else {
			sender.sendPayload(new ClientPackets.SyncFailed(start.registry()));
			var x = RegistrySyncText.missingRegistry(start.registry(), registry != null);
			markDisconnected(handler, x);
			builder.push(x);
		}
	}

	private static void handleDataPacket(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.RegistryData data, PacketSender<CustomPayload> sender) {
		if (currentRegistry == null || syncMap == null) {
			if (optionalRegistry) {
				if (syncMap != null && !syncMap.isEmpty()) {
					var optionalMissing = new IntArrayList(Math.max(currentCount, 0));

					for (var entry : syncMap.values()) {
						for (var value : entry) {
							optionalMissing.add(value.rawId());
						}
					}

					sender.sendPayload(new ClientPackets.UnknownEntry(currentRegistryId, optionalMissing));
				}
			} else {
				LOGGER.warn("Received sync data without specifying registry!");
			}

			return;
		}

		syncMap.putAll(data.packetData());
	}

	@SuppressWarnings("EqualsBetweenInconvertibleTypes")
	private static void handleApplyPacket(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.RegistryApply apply, PacketSender<CustomPayload> sender) {
		if (currentRegistry == null || syncMap == null) {
			if (!optionalRegistry) {
				LOGGER.warn("Received sync data without specifying registry!");
			}

			return;
		}

		var reg = currentRegistry;
		var missingEntries = currentRegistry.quilt$applySyncMap(syncMap);

		if (!optionalRegistry && checkMissingAndDisconnect(handler, currentRegistryId, missingEntries, sender)) {
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
			sender.sendPayload(new ClientPackets.UnknownEntry(currentRegistryId, optionalMissing));
		}

		clearState();
	}

	private static <T, S> ClientConfigurationNetworking.CustomChannelReceiver<ServerPackets.ValidateStates> handleStateValidation(Registry<T> registry, IdList<S> stateList, Function<S, T> converter) {
		return (MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.ValidateStates states, PacketSender<CustomPayload> sender) -> {
			Int2ObjectArrayMap<IntList> data = states.packetData();

			boolean firstMismatch = true;
			for (Int2ObjectMap.Entry<IntList> blockToStates : data.int2ObjectEntrySet()) {
				var block = registry.get(blockToStates.getIntKey());

				for (int stateId : blockToStates.getValue()) {
					var state = stateList.get(stateId);
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

	public static boolean checkMissingAndDisconnect(ClientConfigurationNetworkHandler handler, Identifier registry, Collection<SynchronizedRegistry.MissingEntry> missingEntries, PacketSender<CustomPayload> sender) {
		boolean disconnect = false;

		for (var entry : missingEntries) {
			if (!RegistryFlag.isOptional(entry.flags())) {
				disconnect = true;
				break;
			}
		}

		if (disconnect) {
			if (syncVersion != -1) {
				sender.sendPayload(new ClientPackets.SyncFailed(registry));
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

	private static void markDisconnected(ClientConfigurationNetworkHandler handler, Text reason) {
		if (disconnectMainReason == null) {
			disconnectMainReason = reason;
		}

		mustDisconnect = true;
	}

	private static boolean isTextEmpty(Text text) {
		return (text.asComponent() == TextComponent.EMPTY || (text.asComponent() instanceof LiteralComponent literalComponent && literalComponent.literal().isEmpty())) && text.getSiblings().isEmpty();
	}

	private static void handleRestorePacket(MinecraftClient client, ClientConfigurationNetworkHandler handler, ServerPackets.RegistryRestore restore, PacketSender<CustomPayload> sender) {
		restoreSnapshot(client);
		createSnapshot();
	}

	public static void createSnapshot() {
		for (var reg : Registries.REGISTRY) {
			if (reg instanceof SynchronizedRegistry<?> registry && registry.quilt$requiresSyncing()) {
				registry.quilt$createIdSnapshot();
			}
		}
	}

	public static void restoreSnapshot(MinecraftClient client) {
		for (var reg : Registries.REGISTRY) {
			if (reg instanceof SynchronizedRegistry<?> registry && registry.quilt$requiresSyncing()) {
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
