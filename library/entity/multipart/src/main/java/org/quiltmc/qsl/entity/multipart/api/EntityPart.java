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

package org.quiltmc.qsl.entity.multipart.api;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.MathHelper;

import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Represents the sub-parts of a {@link MultipartEntity}.
 *
 * @param <E> the {@link Entity} that owns this {@link EntityPart}
 * @see EnderDragonPart
 */
public interface EntityPart<E extends Entity> {
	E getOwner();

	/**
	 * Renders the hitbox for the entity part.
	 * <p>
	 * Should normally not be overridden unless it is to more accurately draw non-standard hitboxes.
	 * @param matrices the {@link MatrixStack matrix stack} used for rendering
	 * @param vertices the {@link VertexConsumer vertex consumer} used for rendering
	 * @param ownerX the {@link #getOwner() owner's} rendered X coordinate
	 * @param ownerY the {@link #getOwner() owner's} rendered Y coordinate
	 * @param ownerZ the {@link #getOwner() owner's} rendered Z coordinate
	 * @param owner the {@link #getOwner() owner}
	 * @param tickDelta progress for linearly interpolating between the previous and current game state
	 */
	@ClientOnly
	default void renderHitbox(MatrixStack matrices, VertexConsumer vertices, double ownerX, double ownerY, double ownerZ, Entity owner, float tickDelta) {
		if (this instanceof Entity entityPart) {
			matrices.push();
			double entityPartX = ownerX + MathHelper.lerp(tickDelta, entityPart.lastRenderX, entityPart.getX());
			double entityPartY = ownerY + MathHelper.lerp(tickDelta, entityPart.lastRenderY, entityPart.getY());
			double entityPartZ = ownerZ + MathHelper.lerp(tickDelta, entityPart.lastRenderZ, entityPart.getZ());
			matrices.translate(entityPartX, entityPartY, entityPartZ);
			WorldRenderer.drawBox(matrices, vertices, entityPart.getBoundingBox().offset(-entityPart.getX(), -entityPart.getY(), -entityPart.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
			matrices.pop();
		}
	}
}
