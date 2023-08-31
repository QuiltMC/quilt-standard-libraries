/*
 * Copyright 2021 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.lifecycle.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

@ClientOnly
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
	@Inject(
			method = "run",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;",
					ordinal = 0,
					shift = At.Shift.AFTER
			)
	)
	private void clientReady(CallbackInfo info) {
		ClientLifecycleEvents.READY.invoker().readyClient((MinecraftClient) (Object) this);
	}

	@Inject(
			method = "stop",
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER, remap = false)
	)
	private void clientStopping(CallbackInfo info) {
		ClientLifecycleEvents.STOPPING.invoker().stoppingClient((MinecraftClient) (Object) this);
	}

	@Inject(method = "stop",
			at = {
					@At(value = "INVOKE", target = "Ljava/lang/System;exit(I)V", remap = false), // Graceful JVM Exit
					@At(value = "TAIL") // Final instruction
			})
	private void clientExit(CallbackInfo info) {
		ClientLifecycleEvents.STOPPED.invoker().stoppedClient((MinecraftClient) (Object) this);
	}

	// Ticking

	@Inject(method = "tick", at = @At("HEAD"))
	private void startTick(CallbackInfo info) {
		ClientTickEvents.START.invoker().startClientTick((MinecraftClient) (Object) this);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void endTick(CallbackInfo info) {
		ClientTickEvents.END.invoker().endClientTick((MinecraftClient) (Object) this);
	}
}
