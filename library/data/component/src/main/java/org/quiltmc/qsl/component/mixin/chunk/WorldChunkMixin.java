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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;

import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements ComponentProvider {
	public WorldChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView,
            Registry<Biome> registry, long l, @Nullable ChunkSection[] chunkSections,
            @Nullable BlendingData blendingData) {
		super(chunkPos, upgradeData, heightLimitView, registry, l, chunkSections, blendingData);
	}

	@Inject(
			method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$PostLoadProcessor;)V",
			at = @At("TAIL")
	)
	private void copyComponentData(ServerWorld serverWorld, ProtoChunk protoChunk, WorldChunk.PostLoadProcessor postLoadProcessor, CallbackInfo ci) {
		var target = protoChunk instanceof ReadOnlyChunk readOnly ? readOnly.getWrappedChunk() : protoChunk;
		// FIXME: Does not work and I have no idea why it did up to this point! it still captures the old reference
		LazyComponentContainer.move(
				(LazyComponentContainer) target.getComponentContainer(),
				(LazyComponentContainer) this.getComponentContainer()
		);
	}
}
