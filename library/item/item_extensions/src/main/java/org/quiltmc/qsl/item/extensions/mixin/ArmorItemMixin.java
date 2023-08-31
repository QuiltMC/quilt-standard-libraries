/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.item.extensions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;

/**
 * This mixin allows custom armor materials that have knockback resistance > 0
 * to properly apply that knockback resistance to armor. In vanilla, this is a
 * hardcoded check for the {@link ArmorMaterials#NETHERITE Netherite} material.
 */
@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin {
	@Unique
	private ArmorMaterial quilt$originalArmorMaterial;

	@Inject(
			method = "<init>",
			at = @At(
				value = "FIELD",
				target = "Lnet/minecraft/item/ArmorMaterials;NETHERITE:Lnet/minecraft/item/ArmorMaterials;",
				shift = At.Shift.BEFORE
			)
	)
	private void quilt$captureOriginalMaterial(ArmorMaterial material, ArmorItem.ArmorSlot slot, Item.Settings settings, CallbackInfo ci) {
		this.quilt$originalArmorMaterial = material;
	}

	@ModifyVariable(
			method = "<init>",
			at = @At(
				value = "FIELD",
				target = "Lnet/minecraft/item/ArmorMaterials;NETHERITE:Lnet/minecraft/item/ArmorMaterials;",
				shift = At.Shift.BEFORE
			),
			argsOnly = true
	)
	private ArmorMaterial quilt$applyKnockbackResistanceUnconditionally(ArmorMaterial material) {
		return material.getKnockbackResistance() != 0 ? ArmorMaterials.NETHERITE : this.quilt$originalArmorMaterial;
	}

	@ModifyVariable(
			method = "<init>",
			at = @At(
				value = "FIELD",
				target = "Lnet/minecraft/item/ArmorItem;attributeModifiers:Lcom/google/common/collect/Multimap;"
			),
			argsOnly = true
	)
	private ArmorMaterial quilt$revertMaterialChange(ArmorMaterial material) {
		return this.quilt$originalArmorMaterial;
	}
}
