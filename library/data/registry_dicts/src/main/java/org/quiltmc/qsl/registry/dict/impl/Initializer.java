/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.registry.dict.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.registry.dict.impl.reloader.RegistryDictReloader;

@ApiStatus.Internal
public final class Initializer implements ModInitializer {
	public static final String NAMESPACE = "quilt_registry_dicts";

	public static final String ENABLE_DUMP_BUILTIN_DICTS_CMD_PROPERTY = "quilt.data.registry_dicts.dumpbuiltin_command";

	public static final Logger LOGGER = LoggerFactory.getLogger("QuiltRegistryDicts");

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		RegistryDictReloader.register(ResourceType.SERVER_DATA);

		ServerLifecycleEvents.READY.register(server1 -> server = server1);
		ServerLifecycleEvents.STOPPING.register(server1 -> {
			if (server == server1) {
				server = null;
			}
		});
		ServerPlayConnectionEvents.JOIN.register(Initializer::syncDictsToNewPlayer);

		if (Boolean.getBoolean(ENABLE_DUMP_BUILTIN_DICTS_CMD_PROPERTY)) {
			if (FabricLoader.getInstance().isModLoaded("quilt_command")) {
				DumpBuiltinDictsCommand.register();
			} else {
				LOGGER.warn("Property \"{}\" was set to true, but required module \"quilt_command\" is missing!",
						ENABLE_DUMP_BUILTIN_DICTS_CMD_PROPERTY);
			}
		}
	}

	private static void syncDictsToNewPlayer(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		for (var buf : RegistryDictSync.createSyncPackets()) {
			sender.sendPacket(RegistryDictSync.PACKET_ID, buf);
		}
	}

	public static void resyncDicts() {
		if (server == null) {
			return;
		}

		for (var player : server.getPlayerManager().getPlayerList()) {
			for (var buf : RegistryDictSync.createSyncPackets()) {
				ServerPlayNetworking.send(player, RegistryDictSync.PACKET_ID, buf);
			}
		}
	}
}
