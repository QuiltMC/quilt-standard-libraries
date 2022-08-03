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

package org.quiltmc.qsl.entity.multipart.mixin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.impl.EntityPartTracker;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable, EntityPartTracker {
	@Unique
	private final Int2ObjectMap<Entity> quilt$entityParts = new Int2ObjectOpenHashMap<>();

	@ModifyConstant(
			method = {"m_mbvohlyp", "m_dpwyfaqh", "method_31596", "method_31593"},
			constant = @Constant(classValue = EnderDragonEntity.class, ordinal = 0),
			require = 2,
			remap = false
	)
	private static boolean cancelEnderDragonCheck(Object targetObject, Class<?> classValue) {
		return false;
	}

	@Override
	public Int2ObjectMap<Entity> quilt$getEntityParts() {
		return this.quilt$entityParts;
	}

	/**
	 * Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>
	 * <p>
	 * Allows collecting {@link EntityPart}s that are within the targeted {@link Box}
	 * but are part of {@link Entity entities} in unchecked chunks.
	 */
	@Inject(method = "getOtherEntities", at = @At("RETURN"))
	private void getOtherEntityParts(Entity except, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
		List<Entity> list = cir.getReturnValue();

		// We don't want to check the parts of entities that we already know are invalid
		Set<Entity> skippedOwners = new HashSet<>();

		for (Entity part : this.quilt$getEntityParts().values()) {
			var owner = ((EntityPart<?>) part).getOwner();
			if (skippedOwners.contains(owner) || owner == except) {
				skippedOwners.add(owner);
				continue;
			}

			if (part != except && part.getBoundingBox().intersects(box) && predicate.test(part)) {
				list.add(part);
			}
		}
	}

	/**
	 * Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>
	 * <p>
	 * Allows collecting {@link EntityPart}s that are within the targeted {@link Box}
	 * but are part of {@link Entity entities} in unchecked chunks.
	 */
	@Inject(method = "getEntitiesByType", at = @At("RETURN"))
	private <T extends Entity> void getEntityPartsByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate,
			CallbackInfoReturnable<List<T>> cir) {
		List<T> list = cir.getReturnValue();

		// We don't want to check the parts of entities that we already know are invalid
		Set<Entity> skippedOwners = new HashSet<>();

		for (Entity part : this.quilt$getEntityParts().values()) {
			var owner = ((EntityPart<?>) part).getOwner();
			T entity = filter.downcast(part);

			if (skippedOwners.contains(owner) || filter.downcast(owner) == null || entity == null) {
				skippedOwners.add(owner);
				continue;
			}

			if (entity.getBoundingBox().intersects(box) && predicate.test(entity)) {
				list.add(entity);
			}
		}
	}
}
