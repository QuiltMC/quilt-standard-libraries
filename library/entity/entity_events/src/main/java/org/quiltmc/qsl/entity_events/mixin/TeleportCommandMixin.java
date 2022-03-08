/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.entity_events.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.entity_events.api.EntityWorldChangeEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(TeleportCommand.class)
public abstract class TeleportCommandMixin {
	/**
	 * We need to fire the change world event for entities that are teleported using the `/teleport` command.
	 */
	@SuppressWarnings("InvalidInjectorMethodSignature") // MinecraftDev plugin doesn't understand @Coerce'd parameters
	@Inject(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private static void invokeAfterEntityChangeWorldEvent(ServerCommandSource source, Entity originalEntity, ServerWorld destination, double x, double y, double z, Set<PlayerPositionLookS2CPacket.Flag> movementFlags, float yaw, float pitch, @Coerce /* TeleportCommand.LookTarget */ @Nullable Object facingLocation, CallbackInfo ci, float clampedYaw, float clampedPitch, float h, Entity newEntity) {
		EntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.invoker().afterChangeWorld(originalEntity, newEntity, ((ServerWorld) originalEntity.world), destination);
	}
}
