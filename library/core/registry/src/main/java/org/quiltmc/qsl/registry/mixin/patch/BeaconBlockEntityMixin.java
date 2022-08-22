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

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
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
@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

	@Shadow
	@Final
	private static Set<StatusEffect> EFFECTS;

	@Shadow
	@Nullable StatusEffect primary;

	@Shadow
	@Nullable StatusEffect secondary;

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void quilt$storeIdentifiers(NbtCompound nbt, CallbackInfo ci) {
		if (this.primary != null) {
			nbt.putString(StatusEffectsSerializationConstants.BEACON_PRIMARY_EFFECT_KEY, Registry.STATUS_EFFECT.getId(this.primary).toString());
		}
		if (this.secondary != null) {
			nbt.putString(StatusEffectsSerializationConstants.BEACON_SECONDARY_EFFECT_KEY, Registry.STATUS_EFFECT.getId(this.secondary).toString());
		}
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void quilt$readIdentifiers(NbtCompound compound, CallbackInfo ci) {
		this.primary = this.quilt$readId(StatusEffectsSerializationConstants.BEACON_PRIMARY_EFFECT_KEY, compound, this.primary);
		this.secondary = this.quilt$readId(StatusEffectsSerializationConstants.BEACON_SECONDARY_EFFECT_KEY, compound, this.secondary);
	}

	@Unique
	private @Nullable StatusEffect quilt$readId(String nbtKey, NbtCompound compound, StatusEffect defaultEffect) {
		if (compound.contains(nbtKey, NbtElement.STRING_TYPE)) {
			var identifier = Identifier.tryParse(compound.getString(nbtKey));

			var quiltEffectStatus = Registry.STATUS_EFFECT.get(identifier);

			if (identifier != null && quiltEffectStatus != null && EFFECTS.contains(quiltEffectStatus)) {
				return quiltEffectStatus;
			}
		}

		return defaultEffect;
	}
}
