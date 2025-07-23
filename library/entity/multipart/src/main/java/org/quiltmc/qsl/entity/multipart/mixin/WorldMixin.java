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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.profiler.ProfilerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.AbortableIterationConsumer;
import net.minecraft.util.math.Box;
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
	protected abstract EntityLookup<Entity> getEntityLookup();

	/**
	 * Cancels the Vanilla entity multipart tracking in the {@link World#getOtherEntities(Entity, Box, Predicate)} method,
	 * which is only for the {@link EnderDragonEntity ender dragon}.
	 *
	 * @param instance the world object we're skipping tracking on
	 * @param original the original call
	 * @return an empty immutable list
	 */
	@WrapOperation(
			method = "getOtherEntities",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_65097()Ljava/util/Collection;")
	)
	private static Collection<EnderDragonPart> cancelEnderDragonCheck(
			World instance, Operation<Collection<EnderDragonPart>> original
	) {
		return Collections.emptyList();
	}

	@Override
	public Int2ObjectMap<Entity> quilt$getEntityParts() {
		return this.quilt$entityParts;
	}

	/**
	 * Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>
	 *
	 * <p>Allows collecting {@link EntityPart}s that are within the targeted {@link Box}
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

			if (part != except && part.getBounds().intersects(box) && predicate.test(part)) {
				list.add(part);
			}
		}
	}

	/**
	 * Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>
	 *
	 * <p>Allows collecting {@link EntityPart}s that are within the targeted {@link Box}
	 * but are part of {@link Entity entities} in unchecked chunks.
	 *
	 * @author The Quilt Project, Whangd00dle, LambdAurora (to blame for Overwrite)
	 * @reason Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>, bare injections require a thread
	 * local.
	 */
	@Overwrite
	public <T extends Entity> void collectEntities(
			TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate,
			List<? super T> collection, int maxEntities
	) {
		ProfilerManager.get().visit("getEntities");

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

				if (downcastPart.getBounds().intersects(box) && predicate.test(downcastPart)) {
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
