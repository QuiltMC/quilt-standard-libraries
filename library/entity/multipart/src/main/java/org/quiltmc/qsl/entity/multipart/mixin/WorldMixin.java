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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.AbortableIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.entity.EntityLookup;

import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.impl.EntityPartTracker;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable, EntityPartTracker {
	@Unique
	private final Int2ObjectMap<Entity> quilt$entityParts = new Int2ObjectOpenHashMap<>();

	@Shadow
	public abstract Profiler getProfiler();

	@Shadow
	protected abstract EntityLookup<Entity> getEntityLookup();

	/**
	 * Cancels the Vanilla entity multipart checks in the {@link World#getOtherEntities(Entity, Box, Predicate)} method,
	 * which is an instanceof with the {@link EnderDragonEntity ender dragon}.
	 *
	 * @param targetObject the entity object we're performing the instanceof on
	 * @param classValue   the class the entity is supposed to match
	 * @return {@code false}
	 */
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@ModifyConstant(
			method = "method_31593(Lnet/minecraft/entity/Entity;Ljava/util/function/Predicate;Ljava/util/List;Lnet/minecraft/entity/Entity;)V",
			constant = @Constant(classValue = EnderDragonEntity.class, ordinal = 0)
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
	 *
	 * @author The Quilt Project, Whangd00dle, LambdAurora (to blame for Overwrite)
	 * @reason Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>, bare injections require a thread local.
	 */
	@Overwrite
	public <T extends Entity> void collectEntities(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate,
			List<? super T> collection, int maxEntities) {
		this.getProfiler().visit("getEntities");
		this.getEntityLookup().forEachIntersecting(filter, box, entity -> {
			if (predicate.test(entity)) {
				collection.add(entity);

				if (collection.size() >= maxEntities) {
					return AbortableIterationConsumer.IterationStatus.ABORT;
				}
			}

			/* QUILT START */
			// We don't want to check the parts of entities that we already know are invalid
			Set<Entity> skippedOwners = new HashSet<>();

			for (Entity part : this.quilt$getEntityParts().values()) {
				var owner = ((EntityPart<?>) part).getOwner();
				T downcastPart = filter.downcast(part);

				if (skippedOwners.contains(owner) || filter.downcast(owner) == null || downcastPart == null) {
					skippedOwners.add(owner);
					continue;
				}

				if (downcastPart.getBoundingBox().intersects(box) && predicate.test(downcastPart)) {
					collection.add(downcastPart);

					if (collection.size() >= maxEntities) {
						return AbortableIterationConsumer.IterationStatus.ABORT;
					}
				}
			}
			/* QUILT END */

			return AbortableIterationConsumer.IterationStatus.CONTINUE;
		});
	}
}
