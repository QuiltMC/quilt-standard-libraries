package org.quiltmc.qsl.registry.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.registry.impl.sync.ServerRegistrySync;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = "onPlayerConnect", at = @At("HEAD"))
	private void quilt$sendSync(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		if (!this.server.isHost(player.getGameProfile())) {
			ServerRegistrySync.sendSyncPackets(connection, player);
		}
	}
}
