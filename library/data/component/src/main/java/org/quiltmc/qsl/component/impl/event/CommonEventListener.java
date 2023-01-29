/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.component.impl.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.component.impl.sync.packet.RegistryPacket;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;

public final class CommonEventListener {
	public static void onQueryStart(ServerLoginNetworkHandler ignoredHandler, MinecraftServer ignoredServer, PacketSender sender, ServerLoginNetworking.LoginSynchronizer ignoredSyncer) {
		sender.sendPacket(PacketIds.TYPES, RegistryPacket.createRegistryPacket(Components.REGISTRY));
	}

	public static void onServerStart(MinecraftServer ignored) {
		ComponentsImpl.REGISTRY.freeze();
	}

	public static void onServerTick(MinecraftServer server) {
		server.getComponentContainer().tick(server);
	}

	public static void onServerWorldTick(MinecraftServer ignored, ServerWorld world) {
		world.getComponentContainer().tick(world);
	}
}
