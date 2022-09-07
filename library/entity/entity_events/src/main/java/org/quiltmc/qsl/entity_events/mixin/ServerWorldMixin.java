package org.quiltmc.qsl.entity_events.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.quiltmc.qsl.entity_events.api.ServerEntityTickCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	@Inject(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
	void invokeEntityTickEvent(Entity entity, CallbackInfo ci) {
		ServerEntityTickCallback.ENTITY_TICK.invoker().onServerEntityTick(entity, false);
	}

	@Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickRiding()V"))
	void invokePassengerEntityTickEvent(Entity vehicle, Entity passenger, CallbackInfo ci) {
		ServerEntityTickCallback.ENTITY_TICK.invoker().onServerEntityTick(passenger, true);
	}
}
