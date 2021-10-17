package org.quiltmc.qsl.entity.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.quiltmc.qsl.entity.api.event.EntityLoadEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/world/ServerWorld$ServerEntityHandler")
public abstract class ServerEntityHandlerMixin {
	@SuppressWarnings("ShadowTarget")
	private @Shadow ServerWorld field_26936; // ServerWorld.this

	@Inject(method = "startTracking", at = @At("TAIL"))
	void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
		EntityLoadEvents.AFTER_ENTITY_LOAD.invoker().onLoad(entity, this.field_26936);
	}

	@Inject(method = "stopTracking", at = @At("TAIL"))
	void invokeEntityUnloadEvent(Entity entity, CallbackInfo ci) {
		EntityLoadEvents.AFTER_ENTITY_UNLOAD.invoker().onUnload(entity, this.field_26936);
	}
}
