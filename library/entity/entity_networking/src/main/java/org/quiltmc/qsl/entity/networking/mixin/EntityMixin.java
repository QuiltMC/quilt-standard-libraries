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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.unmapped.C_trgduzfi;

import org.quiltmc.qsl.entity.networking.api.extended_spawn_data.QuiltExtendedSpawnDataEntity;
import org.quiltmc.qsl.entity.networking.impl.QuiltEntityNetworkingInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public abstract int getId();

	@Inject(method = "createSpawnPacket", at = @At("RETURN"), cancellable = true)
	private void quilt$createExtendedSpawnPacket(CallbackInfoReturnable<Packet<ClientPlayPacketListener>> cir) {
		if (this instanceof QuiltExtendedSpawnDataEntity extended) {
			var buf = PacketByteBufs.create();
			buf.writeVarInt(getId());
			extended.writeAdditionalSpawnData(buf);
			var additionalPacket = ServerPlayNetworking.createS2CPacket(
				QuiltEntityNetworkingInitializer.EXTENDED_SPAWN_PACKET_ID, buf
			);
			Packet<ClientPlayPacketListener> basePacket = cir.getReturnValue();
			C_trgduzfi bundlePacket = new C_trgduzfi(List.of(basePacket, additionalPacket));
			cir.setReturnValue(bundlePacket);
		}
	}
}
