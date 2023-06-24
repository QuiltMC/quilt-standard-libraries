/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.registry.mixin.client;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.IdList;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.registry.impl.sync.client.RebuildableIdModelHolder;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedIdList;

@ClientOnly
@Mixin(BlockColors.class)
public class BlockColorMapMixin implements RebuildableIdModelHolder {
	@Final
	@Shadow
	private IdList<BlockColorProvider> providers;

	@Unique
	private final Map<Block, BlockColorProvider> quilt$providers = new Object2ObjectOpenHashMap<>();

	@Inject(method = "registerColorProvider", at = @At("TAIL"))
	private void quilt$storeProviders(BlockColorProvider provider, Block[] blocks, CallbackInfo ci) {
		for (var block : blocks) {
			this.quilt$providers.put(block, provider);
		}
	}

	@Override
	public void quilt$rebuildIds() {
		SynchronizedIdList.clear(this.providers);

		for (var entry : this.quilt$providers.entrySet()) {
			this.providers.set(entry.getValue(), Registries.BLOCK.getRawId(entry.getKey()));
		}
	}
}
