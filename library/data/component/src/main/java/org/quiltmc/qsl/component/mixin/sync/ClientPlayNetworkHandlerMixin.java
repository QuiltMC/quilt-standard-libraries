/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.component.mixin.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.ChunkPos;

import org.quiltmc.qsl.component.api.sync.SyncChannel;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onChunkData", at = @At("RETURN"))
	private void initialChunkSync(ChunkDataS2CPacket packet, CallbackInfo ci) {
		SyncChannel.CHUNK.requestSync(new ChunkPos(packet.chunkX(), packet.chunkZ()));
	}

	@Inject(method = "onEntitySpawn", at = @At("RETURN"))
	private void initialEntitySync(EntitySpawnS2CPacket packet, CallbackInfo ci) {
		SyncChannel.ENTITY.requestSync(packet.getId());
	}
}
