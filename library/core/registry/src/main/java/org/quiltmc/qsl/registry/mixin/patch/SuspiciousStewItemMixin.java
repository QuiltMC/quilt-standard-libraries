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

package org.quiltmc.qsl.registry.mixin.patch;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.api.StatusEffectsSerializationConstants;

/**
 * Modifies storing of status effect to make it more mod friendly.
 * <p>
 * Minecraft by default serializes status effects as raw registry value, which are not stable at all!
 * Raw registry values may change randomly while adding or removing mods, this may create inconsistencies and corruption.
 * Storing the full identifier fixes this issue. This is the last place where the flattening hasn't taken effect.
 */
@Mixin(SuspiciousStewItem.class)
public class SuspiciousStewItemMixin {
	@Unique
	private static final ThreadLocal<StatusEffect> quilt$effect = new ThreadLocal<>();

	@Inject(
			method = "addEffectToStew",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;getRawId(Lnet/minecraft/entity/effect/StatusEffect;)I"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void quilt$storeIdentifier(ItemStack stew, StatusEffect effect, int duration, CallbackInfo ci,
			NbtCompound unused, NbtList unused2, NbtCompound nbt) {
		nbt.putString(StatusEffectsSerializationConstants.EFFECT_ID_KEY, Registries.STATUS_EFFECT.getId(effect).toString());
	}

	@Inject(
			method = "consumeStatusEffects",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;byRawId(I)Lnet/minecraft/entity/effect/StatusEffect;"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void quilt$readCustomEffect(ItemStack stack, Consumer<StatusEffectInstance> consumer, CallbackInfo ci,
			NbtCompound nbtCompound, NbtList nbtList, int i, NbtCompound effectCompound, int j) {
		if (effectCompound.contains(StatusEffectsSerializationConstants.EFFECT_ID_KEY, NbtElement.STRING_TYPE)) {
			var identifier = Identifier.tryParse(effectCompound.getString(StatusEffectsSerializationConstants.EFFECT_ID_KEY));

			if (identifier != null && Registries.STATUS_EFFECT.containsId(identifier)) {
				quilt$effect.set(Registries.STATUS_EFFECT.get(identifier));
			} else {
				quilt$effect.remove();
			}
		} else {
			quilt$effect.remove();
		}
	}

	@ModifyArg(
			method = "consumeStatusEffects",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;<init>(Lnet/minecraft/entity/effect/StatusEffect;I)V"),
			index = 0
	)
	private static StatusEffect quilt$setEffect(StatusEffect effect) {
		var quiltEffect = quilt$effect.get();

		return quiltEffect != null ? quiltEffect : effect;
	}
}
