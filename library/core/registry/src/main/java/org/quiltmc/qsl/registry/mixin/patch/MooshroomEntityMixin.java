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

package org.quiltmc.qsl.registry.mixin.patch;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.api.StatusEffectsSerializationConstants;

/**
 * Modifies storing of status effect to make it more mod friendly.
 * <p>
 * Minecraft by default serializes status effects as raw registry value, which are not stable at all!
 * Raw registry values may change randomly while adding or removing mods, this may create inconsistencies and corruption.
 * Storing the full identifier fixes this issue. This is the last place where the flattening hasn't taken effect.
 */
@Mixin(MooshroomEntity.class)
public class MooshroomEntityMixin {
	@Shadow
	private @Nullable StatusEffect stewEffect;

	@Inject(
			method = "writeCustomDataToNbt",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;getRawId(Lnet/minecraft/entity/effect/StatusEffect;)I")
	)
	private void quilt$storeIdentifier(NbtCompound nbt, CallbackInfo ci) {
		nbt.putString(StatusEffectsSerializationConstants.EFFECT_ID_KEY, Registry.STATUS_EFFECT.getId(this.stewEffect).toString());
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void quilt$readIdentifier(NbtCompound nbt, CallbackInfo ci) {
		if (nbt.contains(StatusEffectsSerializationConstants.EFFECT_ID_KEY, NbtElement.STRING_TYPE)) {
			var identifier = Identifier.tryParse(nbt.getString(StatusEffectsSerializationConstants.EFFECT_ID_KEY));

			if (identifier != null && Registry.STATUS_EFFECT.containsId(identifier)) {
				this.stewEffect = Registry.STATUS_EFFECT.get(identifier);
			}
		}
	}
}
