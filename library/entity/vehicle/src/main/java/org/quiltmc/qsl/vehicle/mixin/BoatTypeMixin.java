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

package org.quiltmc.qsl.vehicle.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.vehicle.BoatEntity;
import org.quiltmc.qsl.vehicle.impl.QuiltBoatTypeRegistryInternals;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(BoatEntity.Type.class)
public class BoatTypeMixin {
	@Invoker("<init>")
	private static BoatEntity.Type newType(String internalName, int ordinal, Block baseBlock, String name){
		throw new AssertionError();
	}

	@Shadow(aliases = "field_7724")
	@Final
	@Mutable
	private static BoatEntity.Type[] VALUES;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void registerEnumExtender(CallbackInfo ci) {
		QuiltBoatTypeRegistryInternals.register((String internalName, Block baseBlock, String name) -> {
			int ordinal = VALUES.length;
			var newValues = Arrays.copyOf(VALUES, ordinal + 1);
			var newValue = newType(internalName, ordinal, baseBlock, name);
			newValues[ordinal] = newValue;
			VALUES = newValues;
			return newValue;
		});
	}
}
