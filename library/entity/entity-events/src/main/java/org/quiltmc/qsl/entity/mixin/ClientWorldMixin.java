package org.quiltmc.qsl.entity.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.quiltmc.qsl.entity.api.event.EntityTickCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	@Inject(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
	void invokeEntityTickEvent(Entity entity, CallbackInfo ci) {
		EntityTickCallback.ENTITY_TICK.invoker().onTick(entity, false, false);
	}

	@Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickRiding()V"))
	void invokePassengerEntityTickEvent(Entity vehicle, Entity passenger, CallbackInfo ci) {
		EntityTickCallback.ENTITY_TICK.invoker().onTick(passenger, false, true);
	}
}
