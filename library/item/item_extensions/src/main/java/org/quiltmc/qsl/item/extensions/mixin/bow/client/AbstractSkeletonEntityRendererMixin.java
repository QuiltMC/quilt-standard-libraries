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

package org.quiltmc.qsl.item.extensions.mixin.bow.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.entity.AbstractSkeletonEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.extensions.api.bow.BowExtensions;

@Mixin(AbstractSkeletonEntityRenderer.class)
abstract class AbstractSkeletonEntityRendererMixin {
	// Allows Skeletons to visually shoot custom bows by returning true
	@Redirect(
			method = {
				"updateState(Lnet/minecraft/entity/mob/AbstractSkeletonEntity;Lnet/minecraft/client/render/entity/"
					+ "state/SkeletonRenderState;F)V",
				"getArmPose(Lnet/minecraft/entity/mob/AbstractSkeletonEntity;Lnet/minecraft/util/Arm;)Lnet/minecraft/"
					+ "client/render/entity/model/BipedEntityModel$ArmPose;"
			},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
	)
	private boolean widenBowClassification(ItemStack heldItemStack, Item item) {
		return heldItemStack.getItem() instanceof BowExtensions;
	}
}
