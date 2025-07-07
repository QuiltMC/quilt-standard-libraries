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

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.Language;

import org.quiltmc.loader.api.minecraft.DedicatedServerOnly;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@DedicatedServerOnly
@Mixin(Language.class)
abstract class LanguageMixin {
	@ModifyArg(
			method = "create",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Map;copyOf(Ljava/util/Map;)Ljava/util/Map;",
					remap = false
			)
	)
	private static Map<String, String> appendEntriesToResourceLoader(Map<String, String> map) {
		ResourceLoaderImpl.appendLanguageEntries(map);
		return map;
	}
}
