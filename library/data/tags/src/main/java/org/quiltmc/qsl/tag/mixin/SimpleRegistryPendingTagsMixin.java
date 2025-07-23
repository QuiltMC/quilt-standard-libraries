/*
 * Copyright 2025 The Quilt Project
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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderSet;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

// C_hicdurkx is the anonymous Registry.PendingTags implementation in SimpleRegistry::startTagReload
@Mixin(targets = {"net.minecraft.registry.SimpleRegistry$C_hicdurkx"})
abstract class SimpleRegistryPendingTagsMixin {
	@WrapOperation(
			// method_63536 is the lambda in bind() passed to Map::forEach
			method = "method_63536",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/HolderSet$NamedSet;bindTo(Ljava/util/List;)V")
	)
	private static void populateTag(
			HolderSet.NamedSet<?> instance, List<Holder<?>> contents, Operation<Void> original,
			Map<TagKey<?>, HolderSet.NamedSet<?>> tags, TagKey<?> key
	) {
		original.call(instance, contents);
		TagRegistryImpl.populateTag(key, contents);
	}
}
