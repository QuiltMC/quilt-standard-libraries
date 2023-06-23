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

package org.quiltmc.qsl.resource.loader.mixin.server;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.util.Language;

import org.quiltmc.loader.api.minecraft.DedicatedServerOnly;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@DedicatedServerOnly
@Mixin(Language.class)
public class LanguageMixin {
	@Redirect(
			method = "create",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;",
					remap = false
			)
	)
	private static ImmutableMap<String, String> create(ImmutableMap.Builder<String, String> builder) {
		var map = new Object2ObjectOpenHashMap<>(builder.buildOrThrow());
		ResourceLoaderImpl.appendLanguageEntries(map);
		return ImmutableMap.copyOf(map);
	}
}
