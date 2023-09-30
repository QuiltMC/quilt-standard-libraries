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

package org.quiltmc.qsl.lifecycle.mixin;

import java.util.Iterator;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
	@Inject(
			method = "runServer",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z")
	)
	private void serverStarting(CallbackInfo info) {
		ServerLifecycleEvents.STARTING.invoker().startingServer((MinecraftServer) (Object) this);
	}

	@Inject(
			method = "runServer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;createServerMetadata()Lnet/minecraft/server/ServerMetadata;",
					ordinal = 0,
					shift = At.Shift.AFTER
			)
	)
	private void serverReady(CallbackInfo info) {
		ServerLifecycleEvents.READY.invoker().readyServer((MinecraftServer) (Object) this);
	}

	@Inject(method = "shutdown", at = @At("HEAD"))
	private void serverStopping(CallbackInfo info) {
		ServerLifecycleEvents.STOPPING.invoker().stoppingServer((MinecraftServer) (Object) this);
	}

	@Inject(method = "shutdown", at = @At("TAIL"))
	private void serverExit(CallbackInfo info) {
		ServerLifecycleEvents.STOPPED.invoker().exitServer((MinecraftServer) (Object) this);
	}

	// Ticking

	@Inject(
			method = "tick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	)
	private void startServerTick(CallbackInfo info) {
		ServerTickEvents.START.invoker().startServerTick((MinecraftServer) (Object) this);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void endServerTick(CallbackInfo info) {
		ServerTickEvents.END.invoker().endServerTick((MinecraftServer) (Object) this);
	}

	// Loading/unloading worlds

	// Yes an Inject could be used for this and it would work with no issues.
	//
	// The reason for a redirect here is the frankly ridiculous amount of local variables that would be captured to obtain
	// the instance of the world being loaded. A redirect does this much more cleanly.
	@Redirect(method = "createWorlds", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <K, V> V loadWorld(Map<K, V> worlds, K key, V world) {
		final V result = worlds.put(key, world);
		ServerWorldLoadEvents.LOAD.invoker().loadWorld((MinecraftServer) (Object) this, (ServerWorld) world);

		return result;
	}

	@Inject(
			method = "shutdown",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;close()V"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void unloadWorld(CallbackInfo info, Iterator<ServerWorld> iterator, ServerWorld world) {
		ServerWorldLoadEvents.UNLOAD.invoker().unloadWorld((MinecraftServer) (Object) this, world);
	}
}
