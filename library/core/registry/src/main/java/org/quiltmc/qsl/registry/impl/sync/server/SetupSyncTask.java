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

import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;

import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;
import org.quiltmc.qsl.registry.impl.sync.ServerPackets;
import org.quiltmc.qsl.registry.mixin.AbstractServerPacketHandlerAccessor;

public record SetupSyncTask(ServerConfigurationPacketHandler handler) implements ConfigurationTask {
	public static final ConfigurationTask.Type TYPE = new Type("qsl:configure_sync");

	@Override
	public void start(Consumer<Packet<?>> task) {
		// First check if Quilt sync is available
		if (ServerConfigurationNetworking.getSendable(this.handler).contains(ServerPackets.Handshake.ID)) {
			((ServerConfigurationTaskManager) this.handler).addTask(new QuiltSyncTask(this.handler, ((AbstractServerPacketHandlerAccessor) this.handler).getConnection()));
		} else if (ServerRegistrySync.forceFabricFallback || (ServerRegistrySync.supportFabric && ServerConfigurationNetworking.getSendable(this.handler).contains(ServerFabricRegistrySync.ID))) {
			FabricSyncTask fabricSyncTask = new FabricSyncTask(this.handler);
			ServerConfigurationNetworking.registerReceiver(this.handler, ServerFabricRegistrySync.SYNC_COMPLETE_ID, (server, handler, buf, responseSender) -> fabricSyncTask.handleComplete());
			((ServerConfigurationTaskManager) this.handler).addTask(fabricSyncTask);
		} else {
			((AbstractServerPacketHandlerAccessor) this.handler).getConnection().disconnect(ServerRegistrySync.noRegistrySyncMessage);
		}

		((ServerConfigurationTaskManager) this.handler).finishTask(TYPE);
	}

	@Override
	public Type getType() {
		return TYPE;
	}
}
