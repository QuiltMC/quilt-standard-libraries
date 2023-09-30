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

package org.quiltmc.qsl.item.extensions.mixin.crossbow;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.RangedWeaponItem;

import org.quiltmc.qsl.item.extensions.api.crossbow.CrossbowExtensions;

// Allows Crossbow users to use custom crossbows
@Mixin({PiglinEntity.class, PillagerEntity.class})
public class EntityUseCrossbowMixin {
	@Inject(method = "canUseRangedWeapon", at = @At("HEAD"), cancellable = true)
	public void canUseRangedWeapon(RangedWeaponItem weapon, CallbackInfoReturnable<Boolean> cir) {
		if (weapon instanceof CrossbowExtensions) {
			cir.setReturnValue(true);
		}
	}
}
