/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.feature.flag.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.feature_flags.FeatureFlagRegistry;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.util.Identifier;

@Mixin(FeatureFlags.class)
public class FeatureFlagsMixin {
	@ModifyVariable(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/feature_flags/FeatureFlagRegistry$Builder;build()Lnet/minecraft/feature_flags/FeatureFlagRegistry;"))
	private static FeatureFlagRegistry.Builder addModdedFlag(FeatureFlagRegistry.Builder builder) {
		builder.createFlag(new Identifier("quilt", "modded"));
		return builder;
	}
}
