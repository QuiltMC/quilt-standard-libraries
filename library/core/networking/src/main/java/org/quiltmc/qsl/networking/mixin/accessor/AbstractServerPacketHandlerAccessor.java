package org.quiltmc.qsl.networking.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.AbstractServerPacketHandler;
import net.minecraft.server.MinecraftServer;

@Mixin(AbstractServerPacketHandler.class)
public interface AbstractServerPacketHandlerAccessor {
	@Accessor("connection")
	ClientConnection getConnection();

	@Accessor("server")
	MinecraftServer getServer();
}
