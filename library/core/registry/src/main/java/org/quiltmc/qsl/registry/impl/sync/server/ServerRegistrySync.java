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

package org.quiltmc.qsl.registry.impl.sync.server;

import static org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.createS2CPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;

import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;
import org.quiltmc.qsl.registry.impl.RegistryConfig;
import org.quiltmc.qsl.registry.impl.sync.ClientPackets;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;
import org.quiltmc.qsl.registry.impl.sync.ServerPackets;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;

@ApiStatus.Internal
public final class ServerRegistrySync {
	private static final int MAX_SAFE_PACKET_SIZE = 734003;

	public static Text noRegistrySyncMessage = Text.empty();
	public static Text errorStyleHeader = Text.empty();
	public static Text errorStyleFooter = Text.empty();
	public static boolean supportFabric = false;
	public static boolean forceFabricFallback = false;
	public static boolean forceDisable = false;
	public static boolean showErrorDetails = false;
	public static boolean stateValidation = true;

	public static IntList SERVER_SUPPORTED_PROTOCOL = new IntArrayList(ProtocolVersions.IMPL_SUPPORTED_VERSIONS);

	public static void registerHandlers() {
		ServerConfigurationNetworking.registerGlobalReceiver(ClientPackets.Handshake.ID, ServerRegistrySync::handleHandshake);
		ServerConfigurationNetworking.registerGlobalReceiver(ClientPackets.SyncFailed.ID, ServerRegistrySync::handleSyncFailed);
		ServerConfigurationNetworking.registerGlobalReceiver(ClientPackets.UnknownEntry.ID, ServerRegistrySync::handleUnknownEntry);
		ServerConfigurationNetworking.registerGlobalReceiver(ClientPackets.ModProtocol.ID, ServerRegistrySync::handleModProtocol);
		ServerConfigurationNetworking.registerGlobalReceiver(ClientPackets.End.ID, ServerRegistrySync::handleEnd);
	}

	public static void handleHandshake(MinecraftServer server, ServerConfigurationPacketHandler handler, ClientPackets.Handshake handshake, PacketSender<CustomPayload> responseSender) {
		((QuiltSyncTask) ((ServerConfigurationTaskManager) handler).getCurrentTask()).handleHandshake(handshake);
	}

	public static void handleSyncFailed(MinecraftServer server, ServerConfigurationPacketHandler handler, ClientPackets.SyncFailed syncFailed, PacketSender<CustomPayload> responseSender) {
		((QuiltSyncTask) ((ServerConfigurationTaskManager) handler).getCurrentTask()).handleSyncFailed(syncFailed);
	}

	public static void handleModProtocol(MinecraftServer server, ServerConfigurationPacketHandler handler, ClientPackets.ModProtocol modProtocol, PacketSender<CustomPayload> responseSender) {
		((QuiltSyncTask) ((ServerConfigurationTaskManager) handler).getCurrentTask()).handleModProtocol(modProtocol);
	}

	public static void handleUnknownEntry(MinecraftServer server, ServerConfigurationPacketHandler handler, ClientPackets.UnknownEntry unknownEntry, PacketSender<CustomPayload> responseSender) {
		((QuiltSyncTask) ((ServerConfigurationTaskManager) handler).getCurrentTask()).handleUnknownEntry(unknownEntry);
	}

	public static void handleEnd(MinecraftServer server, ServerConfigurationPacketHandler handler, ClientPackets.End end, PacketSender<CustomPayload> responseSender) {
		((QuiltSyncTask) ((ServerConfigurationTaskManager) handler).getCurrentTask()).handleEnd(end);
	}

	public static void readConfig() {
		var config = RegistryConfig.INSTANCE.registry_sync;

		noRegistrySyncMessage = text(config.missing_registry_sync_message.value());
		errorStyleHeader = text(config.mismatched_entries_top_message.value());
		errorStyleFooter = text(config.mismatched_entries_bottom_message.value());

		supportFabric = config.support_fabric_api_protocol.value();
		forceFabricFallback = config.force_fabric_api_protocol_fallback.value();
		forceDisable = config.disable_registry_sync.value();
		showErrorDetails = config.mismatched_entries_show_details.value();
		stateValidation = !config.disable_state_validation.value();

		if (stateValidation) {
			for (var container : QuiltLoader.getAllMods()) {
				var data = container.metadata();
				var quiltRegistry = data.value("quilt_registry");

				if (quiltRegistry == null || quiltRegistry.type() != LoaderValue.LType.OBJECT) {
					continue;
				}

				var value = quiltRegistry.asObject().get("disable_state_validation");

				if (value != null && value.type() == LoaderValue.LType.BOOLEAN && value.asBoolean()) {
					stateValidation = false;
					break;
				}
			}
		}

		if (supportFabric) {
			SERVER_SUPPORTED_PROTOCOL.add(ProtocolVersions.FAPI_PROTOCOL);
		}

		if (forceDisable) {
			SERVER_SUPPORTED_PROTOCOL.clear();
		}
	}

	public static void requireMinimumVersion(int version) {
		SERVER_SUPPORTED_PROTOCOL.removeIf(x -> x < version);
	}

	private static Text text(String string) {
		if (string == null || string.isEmpty()) {
			return Text.empty();
		}

		Text text = null;
		try {
			text = Text.Serializer.fromJson(string);
		} catch (Exception e) {}

		return text != null ? text : Text.literal(string);
	}

	public static boolean isNamespaceVanilla(String namespace) {
		return namespace.equals(Identifier.DEFAULT_NAMESPACE) || namespace.equals("brigadier");
	}

	public static boolean shouldSync() {
		if (forceDisable) {
			return false;
		}

		if (ModProtocolImpl.enabled) {
			return true;
		}

		for (var registry : Registries.REGISTRY) {
			if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry
					&& synchronizedRegistry.quilt$requiresSyncing() && synchronizedRegistry.quilt$getContentStatus() != SynchronizedRegistry.Status.VANILLA) {
				return true;
			}
		}

		return false;
	}

	public static boolean requiresSync() {
		if (forceDisable) {
			return false;
		}

		if (!ModProtocolImpl.REQUIRED.isEmpty()) {
			return true;
		}

		for (var registry : Registries.REGISTRY) {
			if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry
					&& synchronizedRegistry.quilt$requiresSyncing() && synchronizedRegistry.quilt$getContentStatus() == SynchronizedRegistry.Status.REQUIRED) {
				return true;
			}
		}

		return false;
	}

	public static void sendSyncPackets(Consumer<Packet<?>> sender, int syncVersion) {
		sendErrorStylePacket(sender);

		if (ModProtocolImpl.enabled) {
			sendModProtocol(sender);
		}

		for (var registry : Registries.REGISTRY) {
			if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry
					&& synchronizedRegistry.quilt$requiresSyncing() && synchronizedRegistry.quilt$getContentStatus() != SynchronizedRegistry.Status.VANILLA) {
				var map = synchronizedRegistry.quilt$getSyncMap();

				var packetData = new HashMap<String, ArrayList<SynchronizedRegistry.SyncEntry>>();

				sendStartPacket(sender, registry);
				int dataLength = 0;

				for (var key : map.keySet()) {
					dataLength += key.length();
					var collection = map.get(key);
					for (var entry : collection) {
						packetData.computeIfAbsent(key, (k) -> new ArrayList<>()).add(entry);
						dataLength += entry.path().length() + 4 + 1;

						if (dataLength > MAX_SAFE_PACKET_SIZE) {
							sendDataPacket(sender, packetData);
							dataLength = 0;
						}
					}

					if (!packetData.isEmpty()) {
						sendDataPacket(sender, packetData);
					}
				}

				sender.accept(createS2CPacket(new ServerPackets.RegistryApply()));
			}
		}

		if (stateValidation) {
			sendStateValidationRequest(sender, ServerPackets.ValidateStates.StateType.BLOCK, Registries.BLOCK, Block.STATE_IDS, block -> block.getStateManager().getStates());
			sendStateValidationRequest(sender, ServerPackets.ValidateStates.StateType.FLUID, Registries.FLUID, Fluid.STATE_IDS, fluid -> fluid.getStateManager().getStates());
		}

		sender.accept(createS2CPacket(new ServerPackets.End()));
	}

	private static <T, B> void sendStateValidationRequest(Consumer<Packet<?>> sender, ServerPackets.ValidateStates.StateType type, Registry<T> registry, IdList<B> stateList, Function<T, Collection<B>> toStates) {
		int dataLength = 0;
		var packetData = new Int2ObjectArrayMap<IntList>();

		for (var key : registry) {
			if (RegistrySynchronization.isEntryOptional((SimpleRegistry<? super T>) registry, key)) {
				continue;
			}

			var blockId = registry.getRawId(key);
			dataLength += VarInts.getSizeBytes(blockId);
			var states = toStates.apply(key);
			var ids = new IntArrayList(states.size());
			packetData.put(blockId, ids);
			dataLength += VarInts.getSizeBytes(states.size());

			for (var entry : states) {
				var stateId = stateList.getRawId(entry);
				dataLength += VarInts.getSizeBytes(stateId);
				ids.add(stateId);

				if (dataLength > MAX_SAFE_PACKET_SIZE) {
					sendStateValidationPacket(sender, type, packetData);
					dataLength = VarInts.getSizeBytes(states.size()) + VarInts.getSizeBytes(blockId);
					ids = new IntArrayList(states.size());
					packetData.put(blockId, ids);
				}
			}
		}

		if (!packetData.isEmpty()) {
			sendStateValidationPacket(sender, type, packetData);
		}
	}

	private static void sendStateValidationPacket(Consumer<Packet<?>> sender, ServerPackets.ValidateStates.StateType type, Int2ObjectArrayMap<IntList> packetData) {
		sender.accept(createS2CPacket(new ServerPackets.ValidateStates(type, new Int2ObjectArrayMap<>(packetData))));
		packetData.clear();
	}

	public static void sendHelloPacket(Consumer<Packet<?>> sender) {
		sender.accept(createS2CPacket(new ServerPackets.Handshake(SERVER_SUPPORTED_PROTOCOL)));
	}

	public static void sendModProtocol(Consumer<Packet<?>> sender) {
		sender.accept(createS2CPacket(new ServerPackets.ModProtocol(ModProtocolImpl.prioritizedId, ModProtocolImpl.ALL)));
	}

	private static void sendErrorStylePacket(Consumer<Packet<?>> sender) {
		sender.accept(createS2CPacket(new ServerPackets.ErrorStyle(ServerRegistrySync.errorStyleHeader, ServerRegistrySync.errorStyleFooter, ServerRegistrySync.showErrorDetails)));
	}

	private static <T extends Registry<?>> void sendStartPacket(Consumer<Packet<?>> sender, T registry) {
		sender.accept(createS2CPacket(new ServerPackets.RegistryStart<T>(registry)));
	}

	private static void sendDataPacket(Consumer<Packet<?>> sender, Map<String, ArrayList<SynchronizedRegistry.SyncEntry>> packetData) {
		sender.accept(createS2CPacket(new ServerPackets.RegistryData(Map.copyOf(packetData))));
		packetData.clear();
	}
}
