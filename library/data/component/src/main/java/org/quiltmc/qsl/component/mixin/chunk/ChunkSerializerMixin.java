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

package org.quiltmc.qsl.component.mixin.chunk;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
	@Inject(method = "deserialize", at = @At("RETURN"), cancellable = true)
	private static void deserializeComponents(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos pos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
		var ret = cir.getReturnValue();
		var target = ret instanceof ReadOnlyChunk readOnly ? readOnly.getWrappedChunk() : ret;
		target.getComponentContainer().readNbt(nbt);
		cir.setReturnValue(ret);
	}

	@Inject(method = "serialize", at = @At("RETURN"), cancellable = true)
	private static void serializeComponents(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
		var ret = cir.getReturnValue();
		chunk.getComponentContainer().writeNbt(ret);
		cir.setReturnValue(ret);
	}
}
