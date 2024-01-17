/*
 * Copyright 2023 The Quilt Project
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

import java.util.function.Consumer;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;

import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;
import org.quiltmc.qsl.registry.impl.sync.ClientPackets;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;

public class QuiltSyncTask implements ConfigurationTask {
	public static final Type TYPE = new Type("qsl:registry_sync");
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ServerConfigurationPacketHandler packetHandler;
	private final ExtendedConnectionClient extendedConnection;
	private Consumer<Packet<?>> sender;
	private int syncVersion = ProtocolVersions.NO_PROTOCOL;

	public QuiltSyncTask(ServerConfigurationPacketHandler packetHandler, ClientConnection connection) {
		this.packetHandler = packetHandler;
		this.extendedConnection = (ExtendedConnectionClient) connection;
	}

	@Override
	public void start(Consumer<Packet<?>> sender) {
		this.sender = sender;
		ServerRegistrySync.sendHelloPacket(sender);
	}

	@Override
	public Type getType() {
		return TYPE;
	}

	private void sendSyncPackets(Consumer<Packet<?>> sender) {
		this.extendedConnection.quilt$setUnderstandsOptional();
		ServerRegistrySync.sendSyncPackets(sender, this.syncVersion);
	}

	public void handleHandshake(ClientPackets.Handshake handshake) {
		this.syncVersion = handshake.version();
		this.sendSyncPackets(this.sender);
	}

	public void handleSyncFailed(ClientPackets.SyncFailed syncFailed) {
		LOGGER.info("Disconnecting {} due to sync failure of {} registry", this.packetHandler.getHost().getName(), syncFailed.registry());
	}

	public void handleModProtocol(ClientPackets.ModProtocol modProtocol) {
		modProtocol.protocols().forEach(this.extendedConnection::quilt$setModProtocol);
	}

	public void handleUnknownEntry(ClientPackets.UnknownEntry unknownEntry) {
		var registry = Registries.REGISTRY.get(unknownEntry.registry());

		unknownEntry.rawIds().forEach(id -> {
			var object = registry.get(id);

			if (object != null) {
				this.extendedConnection.quilt$addUnknownEntry(registry, object);
			}
		});
	}

	public void handleEnd(ClientPackets.End end) {
		if (this.syncVersion == ProtocolVersions.NO_PROTOCOL && ServerRegistrySync.requiresSync()) {
			this.packetHandler.disconnect(ServerRegistrySync.noRegistrySyncMessage);
		} else {
			((ServerConfigurationTaskManager) this.packetHandler).finishTask(TYPE);
		}
	}
}
