package org.quiltmc.qsl.debug_renderers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import org.quiltmc.qsl.debug_renderers.impl.DebugFeatureSync;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void quilt$tellPlayerAboutDebugFeatures(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		DebugFeatureSync.syncFeaturesToClient(player);
	}
}
