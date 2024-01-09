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

import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;

public class FabricSyncTask implements ConfigurationTask {
	public static final Type TYPE = new Type("fabric:registry_sync");
	private final ServerConfigurationPacketHandler packetHandler;

	public FabricSyncTask(ServerConfigurationPacketHandler packetHandler) {
		this.packetHandler = packetHandler;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void start(Consumer<Packet<?>> sender) {
		ServerFabricRegistrySync.sendSyncPackets(sender);
	}

	@Override
	public Type getType() {
		return TYPE;
	}

	public void handleComplete() {
		((ServerConfigurationTaskManager) this.packetHandler).finishTask(TYPE);
	}
}
