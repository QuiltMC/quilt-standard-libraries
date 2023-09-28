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

package org.quiltmc.qsl.tag.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.Holder;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin {
	@Inject(method = "bindTags", at = @At("HEAD"))
	private void onPopulateTags(Map<TagKey<?>, List<Holder<?>>> map, CallbackInfo ci) {
		TagRegistryImpl.populateTags(map);
	}
}
