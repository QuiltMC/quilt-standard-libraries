package org.quiltmc.qsl.lifecycle.mixin.client;

import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

@Mixin(ClientWorld.class)
abstract class ClientWorldMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	// The only client ticking we really care for is the ticking related to (block)entities. So we inject inside of
	// tickEntities.
	//
	// There is a `tick()` method on the client world, but it does so very little (only advancing the time).

	@Inject(method = "tickEntities", at = @At("HEAD"))
	private void startTick(CallbackInfo info) {
		ClientWorldTickEvents.START.invoker().startWorldTick(this.client, (ClientWorld) (Object) this);
	}

	@Inject(method = "tickEntities", at = @At("TAIL"))
	private void endTick(CallbackInfo info) {
		ClientWorldTickEvents.END.invoker().endWorldTick(this.client, (ClientWorld) (Object) this);
	}
}
