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

package org.quiltmc.qsl.component.mixin;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedChunkStorage {

	@Inject(
			method = "sendWatchPackets",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;sendChunkDataPackets(Lnet/minecraft/server/network/ServerPlayerEntity;Lorg/apache/commons/lang3/mutable/MutableObject;Lnet/minecraft/world/chunk/WorldChunk;)V",
					shift = At.Shift.AFTER
			)
	)
	private void syncChunkContainer(ServerPlayerEntity player, ChunkPos pos, MutableObject<ChunkDataS2CPacket> mutableObject, boolean withinMaxWatchDistance, boolean withinViewDistance, CallbackInfo ci) {
		player.getWorld().getChunk(pos.x, pos.z).sync(); // FIXME: Initial chunk sync
	}
}
