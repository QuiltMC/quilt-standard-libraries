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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;

@ClientOnly
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
