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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
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
@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin {
	@Shadow
	@Final
	private StatusEffect type;

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void quilt$storeIdentifier(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		nbt.putString(StatusEffectsSerializationConstants.STATUS_EFFECT_INSTANCE_ID_KEY, Registry.STATUS_EFFECT.getId(this.type).toString());
	}

	@SuppressWarnings({"InvalidInjectorMethodSignature"})
	@ModifyVariable(
			method = "fromNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/effect/StatusEffectInstance;",
			at = @At("STORE"),
			ordinal = 0
	)
	private static StatusEffect quilt$readIdentifier(StatusEffect effect, NbtCompound compound) {
		if (compound.contains(StatusEffectsSerializationConstants.STATUS_EFFECT_INSTANCE_ID_KEY, NbtElement.STRING_TYPE)) {
			var identifier = Identifier.tryParse(compound.getString(StatusEffectsSerializationConstants.STATUS_EFFECT_INSTANCE_ID_KEY));

			if (identifier != null && Registry.STATUS_EFFECT.containsId(identifier)) {
				return Registry.STATUS_EFFECT.get(identifier);
			}
		}

		return effect;
	}
}
