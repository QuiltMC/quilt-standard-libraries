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

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.feature_flags.GatedFeature;

@Mixin(GatedFeature.class)
public interface GatedFeatureMixin {
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Set;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Set;"))
	private static Set<?> makeFilteredRegistriesMutable(Object e1, Object e2, Object e3) {
		return new HashSet<>(Set.of(e1, e2, e3));
	}
}
