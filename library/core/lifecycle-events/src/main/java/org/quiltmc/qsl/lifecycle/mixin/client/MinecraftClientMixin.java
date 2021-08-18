package org.quiltmc.qsl.lifecycle.mixin.client;

import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
	@Inject(method = "run",
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;", shift = At.Shift.AFTER))
	private void clientReady(CallbackInfo info) {
		ClientLifecycleEvents.READY.invoker().readyClient((MinecraftClient) (Object) this);
	}

	@Inject(method = "stop",
		at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER))
	private void clientStopping(CallbackInfo info) {
		ClientLifecycleEvents.STOPPING.invoker().stoppingClient((MinecraftClient) (Object) this);
	}

	@Inject(method = "stop",
		at = {
			@At(value = "INVOKE", target = "Ljava/lang/System;exit(I)V"), // Graceful JVM Exit
			@At(value = "TAIL") // Final instruction
		})
	private void clientExit(CallbackInfo info) {
		ClientLifecycleEvents.STOPPED.invoker().stoppedClient((MinecraftClient) (Object) this);
	}

	// Ticking

	@Inject(at = @At("HEAD"), method = "tick")
	private void startTick(CallbackInfo info) {
		ClientTickEvents.START.invoker().startClientTick((MinecraftClient) (Object) this);
	}

	@Inject(at = @At("RETURN"), method = "tick")
	private void endTick(CallbackInfo info) {
		ClientTickEvents.END.invoker().endClientTick((MinecraftClient) (Object) this);
	}
}
