/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.rendering.entity.mixin.client;

import java.util.EnumMap;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

@Mixin(ArmorMaterials.class)
public abstract class ArmorMaterialsMixin implements ArmorMaterial {
	@Unique
	private /* final */ Identifier quilt$texture;

	@SuppressWarnings("rawtypes")
	@Inject(method = "<init>", at = @At("TAIL"))
	private void quilt$initTexture(String constantName, int ordinal, String name, int j,
								   EnumMap enumMap, int k, SoundEvent soundEvent, float f,
								   float g, Supplier supplier, CallbackInfo ci) {
		this.quilt$texture = new Identifier("textures/models/armor/" + name);
	}

	@Override
	public @NotNull Identifier getTexture() {
		return this.quilt$texture;
	}
}
