/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.entity_events.mixin.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.quiltmc.qsl.entity_events.api.client.ClientEntityLoadEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/world/ClientWorld$ClientEntityHandler")
public abstract class ClientEntityHandlerMixin {
	@SuppressWarnings("ShadowTarget") // MinecraftDev plugin may not be able to resolve this
	@Final @Shadow ClientWorld field_27735; // ClientWorld.this

	@Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
	private void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
		ClientEntityLoadEvents.AFTER_LOAD.invoker().onLoadClient(entity, this.field_27735);
	}

	@Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
	private void invokeEntityUnloadEvent(Entity entity, CallbackInfo ci) {
		ClientEntityLoadEvents.AFTER_UNLOAD.invoker().onUnloadClient(entity, this.field_27735);
	}
}
