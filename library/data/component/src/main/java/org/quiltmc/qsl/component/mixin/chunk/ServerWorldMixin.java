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

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Holder;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;

import org.quiltmc.qsl.component.api.provider.ComponentProvider;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements ComponentProvider {
	protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, Holder<DimensionType> holder, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l, int i) {
		super(mutableWorldProperties, registryKey, holder, supplier, bl, bl2, l, i);
	}

	@Inject(method = "tickChunk", at = @At("TAIL"))
	private void tickChunkContainers(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
		chunk.getComponentContainer().tick(chunk);
	}
}
