package org.quiltmc.qsl.worldgen.dimension.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.quiltmc.qsl.worldgen.dimension.access.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements EntityAccess {

	public TeleportTarget overriddenTeleportTarget = null;

	@Inject(method = "getTeleportTarget(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/world/TeleportTarget;", at = @At("HEAD"), cancellable = true)
	public void getOverriddenTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
		if (overriddenTeleportTarget != null) {
			cir.setReturnValue(overriddenTeleportTarget);
		}
	}

	@Override
	public void setTeleportTarget(TeleportTarget target) {
		overriddenTeleportTarget = target;
	}

}
