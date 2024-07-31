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

package org.quiltmc.qsl.registry.impl.sync;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.Registries;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.quiltmc.qsl.networking.api.server.ServerConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.server.ServerConfigurationTaskManager;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;
import org.quiltmc.qsl.registry.impl.sync.server.ServerRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.server.SetupSyncTask;

@ApiStatus.Internal
public class RegistrySyncInitializer implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		ServerRegistrySync.readConfig();
		ModProtocolImpl.loadVersions();

		SynchronizedRegistry.markForSync(
				Registries.ARMOR_MATERIAL,
				Registries.BLOCK,
				Registries.BLOCK_ENTITY_TYPE,
				Registries.CAT_VARIANT,
				Registries.COMMAND_ARGUMENT_TYPE,
				Registries.CUSTOM_STAT,
				Registries.DATA_COMPONENT_TYPE,
				Registries.ENTITY_ATTRIBUTE,
				Registries.ENTITY_TYPE,
				Registries.FLUID,
				Registries.FROG_VARIANT,
				Registries.GAME_EVENT,
				Registries.ITEM,
				Registries.NUMBER_FORMAT_TYPE,
				Registries.MAP_DECORATION_TYPE,
				Registries.PARTICLE_TYPE,
				Registries.POSITION_SOURCE_TYPE,
				Registries.POTION,
				Registries.RECIPE_SERIALIZER,
				Registries.SCREEN_HANDLER_TYPE,
				Registries.SOUND_EVENT,
				Registries.STAT_TYPE,
				Registries.STATUS_EFFECT,
				Registries.VILLAGER_TYPE,
				Registries.VILLAGER_PROFESSION
		);

		ServerConfigurationConnectionEvents.INIT.register((handler, server) -> {
			((ServerConfigurationTaskManager) handler).addPriorityTask(new SetupSyncTask(handler));
		});

		ServerRegistrySync.registerHandlers();
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.Handshake.ID, ServerPackets.Handshake.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.End.ID, ServerPackets.End.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.RegistryStart.ID, ServerPackets.RegistryStart.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.RegistryData.ID, ServerPackets.RegistryData.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.RegistryApply.ID, ServerPackets.RegistryApply.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.ValidateStates.StateType.BLOCK.packetId(), ServerPackets.ValidateStates.CODEC_BLOCK);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.ValidateStates.StateType.FLUID.packetId(), ServerPackets.ValidateStates.CODEC_FLUID);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.RegistryRestore.ID, ServerPackets.RegistryRestore.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.ErrorStyle.ID, ServerPackets.ErrorStyle.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ServerPackets.ModProtocol.ID, ServerPackets.ModProtocol.CODEC);

		PayloadTypeRegistry.configurationC2S().register(ClientPackets.Handshake.ID, ClientPackets.Handshake.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ClientPackets.SyncFailed.ID, ClientPackets.SyncFailed.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ClientPackets.UnknownEntry.ID, ClientPackets.UnknownEntry.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ClientPackets.ModProtocol.ID, ClientPackets.ModProtocol.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ClientPackets.End.ID, ClientPackets.End.CODEC);
	}
}
