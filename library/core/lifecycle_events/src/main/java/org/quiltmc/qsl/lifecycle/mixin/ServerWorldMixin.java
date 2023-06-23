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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin {
	@Shadow
	public abstract MinecraftServer getServer();

	@Inject(method = "tick", at = @At("HEAD"))
	private void startTick(CallbackInfo info) {
		ServerWorldTickEvents.START.invoker().startWorldTick(this.getServer(), (ServerWorld) (Object) this);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void endServerTick(CallbackInfo info) {
		ServerWorldTickEvents.END.invoker().endWorldTick(this.getServer(), (ServerWorld) (Object) this);
	}
}
