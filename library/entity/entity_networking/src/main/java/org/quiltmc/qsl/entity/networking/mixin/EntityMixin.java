/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.entity.networking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

import org.quiltmc.qsl.entity.networking.api.extended_spawn_data.QuiltExtendedSpawnDataEntity;
import org.quiltmc.qsl.entity.networking.impl.QuiltEntityNetworkingInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "createSpawnPacket", at = @At("HEAD"), cancellable = true)
	private void quilt$createExtendedSpawnPacket(CallbackInfoReturnable<Packet<ClientPlayPacketListener>> cir) {
		if (this instanceof QuiltExtendedSpawnDataEntity extended) {
			var buf = PacketByteBufs.create();
			new EntitySpawnS2CPacket((Entity) (Object) this).write(buf);
			extended.writeAdditionalSpawnData(buf);
			var packet = ServerPlayNetworking.createS2CPacket(
				QuiltEntityNetworkingInitializer.EXTENDED_SPAWN_PACKET, buf
			);
			cir.setReturnValue(packet);
		}
	}
}
