/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.rendering.entity.test.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;

// TODO move this mixin to an item module
@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin {
	@ModifyVariable(method = "<init>",
			at = @At(value = "INVOKE_ASSIGN",
					target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;",
					ordinal = 1,
					remap = false),
			argsOnly = true)
	private ArmorMaterial fixNonNetheriteKnockbackResistance(ArmorMaterial original) {
		if (original.getKnockbackResistance() == 0) {
			return original;
		} else {
			// knockback resistance is only applied to Netherite. for some reason.
			return ArmorMaterials.NETHERITE;
		}
	}
}
