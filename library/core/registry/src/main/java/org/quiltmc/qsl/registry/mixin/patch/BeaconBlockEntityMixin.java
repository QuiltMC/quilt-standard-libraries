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

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Modify storing of status effect to make it more mod friendly
 * <p>
 * Minecraft by default serializes status effects as raw registry value limited to a byte!
 * Which isn't great mod compatibility wise (raw ids shouldn't be considered stable)
 * and limits to supporting only 256 status effects globally!
 */
@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

	@Shadow
	@Final
	private static Set<StatusEffect> EFFECTS;
	@Shadow
	@Nullable
	private StatusEffect primary;
	@Shadow
	@Nullable
	private StatusEffect secondary;

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void quilt$storeIdentifiers(NbtCompound nbt, CallbackInfo ci) {
		if (this.primary != null) {
			nbt.putString("quilt:primary_status", Registry.STATUS_EFFECT.getId(this.primary).toString());
		}
		if (this.secondary != null) {
			nbt.putString("quilt:secondary_status", Registry.STATUS_EFFECT.getId(this.secondary).toString());
		}
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void quilt$readIdentifiers(NbtCompound compound, CallbackInfo ci) {
		this.primary = this.quilt$readId("quilt:primary_status", compound, this.primary);
		this.secondary = this.quilt$readId("quilt:secondary_status", compound, this.secondary);
	}

	@Nullable
	@Unique
	private StatusEffect quilt$readId(String nbtKey, NbtCompound compound, StatusEffect defaultEffect) {
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
