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

package org.quiltmc.qsl.multipart.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.quiltmc.qsl.multipart.api.EntityPart;
import org.quiltmc.qsl.multipart.impl.EntityPartTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable, EntityPartTracker {
	@Unique
	private final Int2ObjectMap<Entity> quilt$entityParts = new Int2ObjectOpenHashMap<>();

	@Override
	public Int2ObjectMap<Entity> getEntityParts() {
		return quilt$entityParts;
	}

	@Redirect(method = {"m_mbvohlyp", "m_dpwyfaqh"}, at=@At(value = "CONSTANT", args = "classValue=net/minecraft/entity/boss/dragon/EnderDragonEntity", ordinal = 0))
	private static boolean cancelEnderDragonCheck(Object targetObject, Class<?> classValue) {
		return false;
	}

	/**
	 * Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>
	 * <p>Allows collecting {@link EntityPart}s that are within the targeted {@link Box}
	 * but are part of {@link Entity entities} in unchecked chunks.</p>
	 */
	@Inject(method = "getOtherEntities", at = @At("RETURN"))
	void getOtherEntityParts(Entity except, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
		List<Entity> list = cir.getReturnValue();
		for (Entity part : this.getEntityParts().values()) {
			if (part != except && part.getBoundingBox().intersects(box) && predicate.test(part)) {
				list.add(part);
			}
		}
	}

	/**
	 * Fixes <a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a>
	 * <p>Allows collecting {@link EntityPart}s that are within the targeted {@link Box}
	 * but are part of {@link Entity entities} in unchecked chunks.</p>
	 */
	@Inject(method = "getEntitiesByType", at = @At("RETURN"))
	<T extends Entity> void getEntityPartsByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, CallbackInfoReturnable<List<T>> cir) {
		List<T> list = cir.getReturnValue();
		for (Entity part : this.getEntityParts().values()) {
			T entity = filter.downcast(part);
			if (entity != null && entity.getBoundingBox().intersects(box) && predicate.test(entity)) {
				list.add(entity);
			}
		}
	}
}
