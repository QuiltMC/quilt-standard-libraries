package org.quiltmc.qsl.entity.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.quiltmc.qsl.entity.api.event.EntityLoadEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/world/ClientWorld$ClientEntityHandler")
public abstract class ClientEntityHandlerMixin {
	@Final @Shadow ClientWorld field_27735; // ClientWorld.this

	@Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
	void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
		EntityLoadEvents.AFTER_ENTITY_LOAD_CLIENT.invoker().onLoad(entity, this.field_27735);
	}

	@Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
	void invokeEntityUnloadEvent(Entity entity, CallbackInfo ci) {
		EntityLoadEvents.AFTER_ENTITY_UNLOAD_CLIENT.invoker().onUnload(entity, this.field_27735);
	}
}
