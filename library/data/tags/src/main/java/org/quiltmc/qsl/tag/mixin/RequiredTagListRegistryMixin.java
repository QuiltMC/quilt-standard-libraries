/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.tag.mixin;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tag.RequiredTagList;
import net.minecraft.tag.RequiredTagListRegistry;

@Mixin(RequiredTagListRegistry.class)
public class RequiredTagListRegistryMixin {
	@Shadow
	@Final
	private static List<RequiredTagList<?>> ALL;

	@Inject(method = "getBuiltinTags", at = @At("TAIL"), cancellable = true)
	private static void onGetBuiltinTags(CallbackInfoReturnable<Set<RequiredTagList<?>>> cir) {
		// Add tag lists registered in QSL to the map.
		var set = ImmutableSet.<RequiredTagList<?>>builder();
		set.addAll(cir.getReturnValue());
		set.addAll(ALL);
		cir.setReturnValue(set.build());
	}
}
