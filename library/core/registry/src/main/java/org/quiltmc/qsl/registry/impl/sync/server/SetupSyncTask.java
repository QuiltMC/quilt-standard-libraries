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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;

import org.quiltmc.qsl.networking.api.server.ServerConfigurationTaskManager;
import org.quiltmc.qsl.registry.mixin.AbstractServerPacketHandlerAccessor;

public record SetupSyncTask(ServerConfigurationNetworkHandler handler) implements ConfigurationTask {
	public static final ConfigurationTask.Type TYPE = new Type("qsl:configure_sync");

	public record SyncTask(String name, Predicate<ServerConfigurationNetworkHandler> shouldRun, Consumer<ServerConfigurationNetworkHandler> setupSync) {}

	public static final int QUILT_SYNC_PRIORITY = 0;
	public static final Int2ObjectMap<SyncTask> SYNC_TASKS = new Int2ObjectArrayMap<>();

	public static void registerSyncTask(int priority, SyncTask syncTask) {
		SYNC_TASKS.put(priority, syncTask);
	}

	@Override
	public void start(Consumer<Packet<?>> task) {
		if (!((AbstractServerPacketHandlerAccessor) this.handler).invokeIsHost() && ServerRegistrySync.shouldSync()) {
			boolean synced = false;

			List<SyncTask> sortedTasks = SYNC_TASKS.int2ObjectEntrySet().stream().sorted().map(Map.Entry::getValue).toList();

			for (SyncTask syncTask : sortedTasks) {
				if (syncTask.shouldRun().test(this.handler)) {
					syncTask.setupSync().accept(this.handler);
					synced = true;
					break;
				}
			}

			if (!synced && ServerRegistrySync.requiresSync()) {
				((AbstractServerPacketHandlerAccessor) this.handler).getConnection().disconnect(ServerRegistrySync.noRegistrySyncMessage);
			}
		}

		((ServerConfigurationTaskManager) this.handler).finishTask(TYPE);
	}

	@Override
	public Type getType() {
		return TYPE;
	}
}
