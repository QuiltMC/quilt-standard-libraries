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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;

import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;
import org.quiltmc.qsl.registry.impl.RegistryConfig;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;
import org.quiltmc.qsl.registry.impl.sync.ServerPackets;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolDef;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;
import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
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

	public static void readConfig() {
		/*var config = RegistryConfig.INSTANCE.registry_sync;

		noRegistrySyncMessage = text(config.missing_registry_sync_message);
		errorStyleHeader = text(config.mismatched_entries_top_message);
		errorStyleFooter = text(config.mismatched_entries_bottom_message);

		supportFabric = config.support_fabric_api_protocol;
		forceFabricFallback = config.force_fabric_api_protocol_fallback;
		forceDisable = config.disable_registry_sync;
		showErrorDetails = config.mismatched_entries_show_details;
		*/
		noRegistrySyncMessage = text((String) RegistryConfig.getSync("missing_registry_sync_message"));
		errorStyleHeader = text((String) RegistryConfig.getSync("mismatched_entries_top_message"));
		errorStyleFooter = text((String) RegistryConfig.getSync("mismatched_entries_bottom_message"));

		supportFabric = (Boolean) RegistryConfig.getSync("support_fabric_api_protocol");
		forceFabricFallback = (Boolean) RegistryConfig.getSync("force_fabric_api_protocol_fallback");
		forceDisable = (Boolean) RegistryConfig.getSync("disable_registry_sync");
		showErrorDetails = (Boolean) RegistryConfig.getSync("mismatched_entries_show_details");
		stateValidation = !(Boolean) RegistryConfig.getSync("disable_state_validation");

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

	public static void sendSyncPackets(ClientConnection connection, ServerPlayerEntity player, int syncVersion) {
		sendErrorStylePacket(connection);

		if (ModProtocolImpl.enabled) {
			sendModProtocol(connection);
		}

		for (var registry : Registries.REGISTRY) {
			if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry
					&& synchronizedRegistry.quilt$requiresSyncing() && synchronizedRegistry.quilt$getContentStatus() != SynchronizedRegistry.Status.VANILLA) {
				var map = synchronizedRegistry.quilt$getSyncMap();

				var packetData = new HashMap<String, ArrayList<SynchronizedRegistry.SyncEntry>>();

				sendStartPacket(connection, registry);
				int dataLength = 0;

				for (var key : map.keySet()) {
					dataLength += key.length();
					var collection = map.get(key);
					for (var entry : collection) {
						packetData.computeIfAbsent(key, (k) -> new ArrayList<>()).add(entry);
						dataLength += entry.path().length() + 4 + 1;

						if (dataLength > MAX_SAFE_PACKET_SIZE) {
							sendDataPacket(connection, packetData);
							dataLength = 0;
						}
					}

					if (packetData.size() > 0) {
						sendDataPacket(connection, packetData);
					}
				}

				connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.REGISTRY_APPLY, PacketByteBufs.empty()));
			}
		}

		if (stateValidation) {
			sendStateValidationRequest(connection, ServerPackets.VALIDATE_BLOCK_STATES, Registries.BLOCK, Block.STATE_IDS, block -> block.getStateManager().getStates());
			sendStateValidationRequest(connection, ServerPackets.VALIDATE_FLUID_STATES, Registries.FLUID, Fluid.STATE_IDS, fluid -> fluid.getStateManager().getStates());
		}

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.END, PacketByteBufs.empty()));
	}

	private static <T, B> void sendStateValidationRequest(ClientConnection connection, Identifier packetId, Registry<T> registry, IdList<B> stateList, Function<T, Collection<B>> toStates) {
		int dataLength = 0;
		var packetData = new Int2ObjectArrayMap<IntList>();

		for (var key : registry) {
			if (RegistrySynchronization.isEntryOptional((SimpleRegistry<? super T>) registry, key)) {
				continue;
			}

			var blockId = registry.getRawId(key);
			dataLength += PacketByteBuf.getVarIntLength(blockId);
			var states = toStates.apply(key);
			var ids = new IntArrayList(states.size());
			packetData.put(blockId, ids);
			dataLength += PacketByteBuf.getVarIntLength(states.size());

			for (var entry : states) {
				var stateId = stateList.getRawId(entry);
				dataLength += PacketByteBuf.getVarIntLength(stateId);
				ids.add(stateId);

				if (dataLength > MAX_SAFE_PACKET_SIZE) {
					sendStateValidationPacket(connection, packetId, packetData);
					dataLength = PacketByteBuf.getVarIntLength(states.size()) + PacketByteBuf.getVarIntLength(blockId);
					ids = new IntArrayList(states.size());
					packetData.put(blockId, ids);
				}
			}
		}

		if (packetData.size() > 0) {
			sendStateValidationPacket(connection, packetId, packetData);
		}
	}

	private static void sendStateValidationPacket(ClientConnection connection, Identifier packetId, Int2ObjectArrayMap<IntList> packetData) {
		var buf = PacketByteBufs.create();

		// Number of namespaces
		buf.writeVarInt(packetData.size());
		for (var entry : packetData.int2ObjectEntrySet()) {
			buf.writeVarInt(entry.getIntKey());
			buf.writeIntList(entry.getValue());
		}

		connection.send(ServerPlayNetworking.createS2CPacket(packetId, buf));

		packetData.clear();
	}

	public static void sendHelloPacket(ClientConnection connection) {
		var buf = PacketByteBufs.create();

		buf.writeIntList(SERVER_SUPPORTED_PROTOCOL);

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.HANDSHAKE, buf));
	}

	public static void sendModProtocol(ClientConnection connection) {
		var buf = PacketByteBufs.create();
		buf.writeString(ModProtocolImpl.prioritizedId);
		buf.writeCollection(ModProtocolImpl.ALL, ModProtocolDef::write);
		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.MOD_PROTOCOL, buf));
	}

	private static void sendErrorStylePacket(ClientConnection connection) {
		var buf = PacketByteBufs.create();

		buf.writeText(ServerRegistrySync.errorStyleHeader);
		buf.writeText(ServerRegistrySync.errorStyleFooter);
		buf.writeBoolean(ServerRegistrySync.showErrorDetails);

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.ERROR_STYLE, buf));
	}

	@SuppressWarnings("unchecked")
	private static <T extends Registry<?>> void sendStartPacket(ClientConnection connection, T registry) {
		var buf = PacketByteBufs.create();

		// Registry id
		buf.writeIdentifier(((Registry<T>) Registries.REGISTRY).getId(registry));

		// Number of entries
		buf.writeVarInt(registry.size());

		// Registry flags
		var flag = ((SynchronizedRegistry<T>) registry).quilt$getRegistryFlag();
		if (((SynchronizedRegistry<T>) registry).quilt$getContentStatus() == SynchronizedRegistry.Status.OPTIONAL) {
			flag |= (0x1 << RegistryFlag.OPTIONAL.ordinal());
		}

		buf.writeByte(flag);

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.REGISTRY_START, buf));
	}

	private static void sendDataPacket(ClientConnection connection, Map<String, ArrayList<SynchronizedRegistry.SyncEntry>> packetData) {
		var buf = PacketByteBufs.create();

		// Number of namespaces
		buf.writeVarInt(packetData.size());
		for (var key : packetData.keySet()) {
			var list = packetData.get(key);

			// Namespace
			buf.writeString(key);

			// Number of entries
			buf.writeVarInt(list.size());

			for (var entry : list) {
				// Path
				buf.writeString(entry.path());
				// Raw id from registry
				buf.writeVarInt(entry.rawId());
				// Entry flags
				buf.writeByte(entry.flags());
			}
		}

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.REGISTRY_DATA, buf));

		packetData.clear();
	}
}
