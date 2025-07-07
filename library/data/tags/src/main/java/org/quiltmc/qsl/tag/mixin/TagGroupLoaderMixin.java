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

package org.quiltmc.qsl.tag.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.registry.Holder;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

@Mixin(TagGroupLoader.class)
public class TagGroupLoaderMixin {
	@WrapOperation(
			// method_61314 is the lambda in TagGroupLoader::bind
			method = "method_61314",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/registry/MutableRegistry;bindTag"
					+ "(Lnet/minecraft/registry/tag/TagKey;Ljava/util/List;)V"
			)
	)
	private static void populateTag(
			MutableRegistry<?> instance, TagKey<?> key, List<Holder<?>> contents, Operation<Void> original
	) {
		original.call(instance, key, contents);
		TagRegistryImpl.populateTag(key, contents);
	}
}
