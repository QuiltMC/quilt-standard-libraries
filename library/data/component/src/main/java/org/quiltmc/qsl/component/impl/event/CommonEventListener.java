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
