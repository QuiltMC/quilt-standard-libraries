package org.quiltmc.qsl.entity.multipart.mixin.client;

import com.google.common.collect.ImmutableList;
import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.api.MultipartEntity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.Hitbox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin<T extends Entity> {
	@ModifyReceiver(
		// method_68835 is createHitboxView
		method = "method_68835",
		at = @At(
			value = "INVOKE", remap = false,
			target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;"
		)
	)
	private ImmutableList.Builder<Hitbox> addMultipartHitboxes(
		ImmutableList.Builder<Hitbox> hitboxes,
		T entity, float tickDelta, boolean green
	) {
		if (entity instanceof MultipartEntity multipartEntity) {
			final double entityX = -MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
			final double entityY = -MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
			final double entityZ = -MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

			for (final EntityPart<?> part : multipartEntity.getEntityParts()) {
				part.getHitbox(entityX, entityY, entityZ, entity, tickDelta).ifPresent(hitboxes::add);
			}
		}

		return hitboxes;
	}
}
