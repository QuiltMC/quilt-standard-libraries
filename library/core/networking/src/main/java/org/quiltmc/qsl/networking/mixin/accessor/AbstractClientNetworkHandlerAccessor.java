package org.quiltmc.qsl.networking.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.network.AbstractClientNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.AbstractServerPacketHandler;
import net.minecraft.server.MinecraftServer;

@Mixin(AbstractClientNetworkHandler.class)
public interface AbstractClientNetworkHandlerAccessor {
	@Accessor("connection")
	ClientConnection getConnection();
}
