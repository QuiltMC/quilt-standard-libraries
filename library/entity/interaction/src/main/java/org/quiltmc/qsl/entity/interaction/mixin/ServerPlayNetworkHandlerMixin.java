package org.quiltmc.qsl.entity.interaction.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.qsl.entity.interaction.api.player.UseEntityCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/network/ServerPlayNetworkHandler$C_wsexhymd")
public abstract class ServerPlayNetworkHandlerMixin implements PlayerInteractEntityC2SPacket.Handler {

	@Shadow
	public ServerPlayNetworkHandler field_28963;

	@Shadow
	public Entity field_28962;

	@Inject(method = "interactAt(Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
	private void onPlayerInteractEntity(Hand hand, Vec3d hitPosition, CallbackInfo ci) {
		EntityHitResult hitResult = new EntityHitResult(field_28962, hitPosition.add(field_28962.getPos()));

		ActionResult result = UseEntityCallback.EVENT.invoker().onUseEntity(field_28963.player, field_28963.player.world, hand, field_28962, hitResult);

		if (result != ActionResult.PASS) ci.cancel();
	}
}
