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

package org.quiltmc.qsl.entity.multipart.mixin;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Holder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import org.quiltmc.qsl.entity.multipart.impl.EntityPartTracker;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements EntityPartTracker {
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryKey, DynamicRegistryManager registryManager,
			Holder<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long l, int i) {
		super(properties, registryKey, registryManager, dimension, profiler, isClient, debugWorld, l, i);
	}

	@Redirect(
			method = "getDragonPart(I)Lnet/minecraft/entity/Entity;",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;dragonParts:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", opcode = Opcodes.GETFIELD)
	)
	private Int2ObjectMap<Entity> quilt$getPart(ServerWorld world) {
		return this.quilt$getEntityParts();
	}
}
