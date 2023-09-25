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
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.registry.impl.sync.client.ClientFabricRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.client.ClientRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;
import org.quiltmc.qsl.registry.impl.sync.server.ServerFabricRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.server.ServerRegistrySync;

@ApiStatus.Internal
public class RegistrySyncInitializer implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		ServerRegistrySync.readConfig();
		ModProtocolImpl.loadVersions();

		SynchronizedRegistry.markForSync(
				Registries.BLOCK,
				Registries.BLOCK_ENTITY_TYPE,
				Registries.CAT_VARIANT,
				Registries.COMMAND_ARGUMENT_TYPE,
				Registries.ENCHANTMENT,
				Registries.ENTITY_TYPE,
				Registries.FLUID,
				Registries.FROG_VARIANT,
				Registries.GAME_EVENT,
				Registries.ITEM,
				Registries.PAINTING_VARIANT,
				Registries.PARTICLE_TYPE,
				Registries.SCREEN_HANDLER_TYPE,
				Registries.SOUND_EVENT,
				Registries.STAT_TYPE,
				Registries.STATUS_EFFECT,
				Registries.VILLAGER_TYPE,
				Registries.VILLAGER_PROFESSION
		);

		ServerRegistrySync.registerHandlers();
		CustomPayloads.registerS2CPayload(ServerPackets.HANDSHAKE, ServerPackets.Handshake::new);
		CustomPayloads.registerS2CPayload(ServerPackets.END, ServerPackets.End::new);
		CustomPayloads.registerS2CPayload(ServerPackets.REGISTRY_START, ServerPackets.RegistryStart::new);
		CustomPayloads.registerS2CPayload(ServerPackets.REGISTRY_DATA, ServerPackets.RegistryData::new);
		CustomPayloads.registerS2CPayload(ServerPackets.REGISTRY_APPLY, ServerPackets.RegistryApply::new);
		CustomPayloads.registerS2CPayload(ServerPackets.VALIDATE_BLOCK_STATES, ServerPackets.ValidateStates::newBlock);
		CustomPayloads.registerS2CPayload(ServerPackets.VALIDATE_FLUID_STATES, ServerPackets.ValidateStates::newFluid);
		CustomPayloads.registerS2CPayload(ServerPackets.REGISTRY_RESTORE, ServerPackets.RegistryRestore::new);
		CustomPayloads.registerS2CPayload(ServerPackets.ERROR_STYLE, ServerPackets.ErrorStyle::new);
		CustomPayloads.registerS2CPayload(ServerPackets.MOD_PROTOCOL, ServerPackets.ModProtocol::new);

		CustomPayloads.registerC2SPayload(ClientPackets.HANDSHAKE, ClientPackets.Handshake::new);
		CustomPayloads.registerC2SPayload(ClientPackets.SYNC_FAILED, ClientPackets.SyncFailed::new);
		CustomPayloads.registerC2SPayload(ClientPackets.UNKNOWN_ENTRY, ClientPackets.UnknownEntry::new);
		CustomPayloads.registerC2SPayload(ClientPackets.MOD_PROTOCOL, ClientPackets.ModProtocol::new);
		CustomPayloads.registerC2SPayload(ClientPackets.END, ClientPackets.End::new);

//		ServerFabricRegistrySync.registerHandlers();
	}
}
