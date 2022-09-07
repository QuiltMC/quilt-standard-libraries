package org.quiltmc.qsl.entity_events.mixin.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.quiltmc.qsl.entity_events.api.client.ClientEntityTickCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	@Inject(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
	void invokeEntityTickEvent(Entity entity, CallbackInfo ci) {
		ClientEntityTickCallback.ENTITY_TICK.invoker().onClientEntityTick(entity, false);
	}

	@Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickRiding()V"))
	void invokePassengerEntityTickEvent(Entity vehicle, Entity passenger, CallbackInfo ci) {
		ClientEntityTickCallback.ENTITY_TICK.invoker().onClientEntityTick(passenger, true);
	}
}
