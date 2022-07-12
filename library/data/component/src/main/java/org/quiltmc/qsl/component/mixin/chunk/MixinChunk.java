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

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;
import org.quiltmc.qsl.component.impl.sync.SyncPlayerList;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public abstract class MixinChunk implements ComponentProvider {
	private ComponentContainer qsl$container;

	@Shadow
	public abstract void setNeedsSaving(boolean needsSaving);

	@Override
	public ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<?> registry, long l, ChunkSection[] chunkSections, BlendingData blendingData, CallbackInfo ci) {
		LazyComponentContainer.Builder builder = ComponentContainer.builder(this)
				.unwrap()
				.saving(() -> this.setNeedsSaving(true));

		if ((Chunk) (Object) this instanceof WorldChunk worldChunk) {
			builder.syncing(SyncPacketHeader.CHUNK, () -> SyncPlayerList.create(worldChunk)).ticking();
		}

		this.qsl$container = builder.build(LazyComponentContainer.FACTORY);
	}
}
