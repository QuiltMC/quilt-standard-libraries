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

package org.quiltmc.qsl.resource.loader.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.pack.ResourcePackCompatibility;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.ResourcePackSource;
import net.minecraft.text.Text;

import org.quiltmc.qsl.resource.loader.api.QuiltResourcePackProfile;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

@Mixin(ResourcePackProfile.class)
public class ResourcePackProfileMixin implements QuiltResourcePackProfile {
	@Unique
	private ResourcePackActivationType quilt$activationType;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void quilt$onInit(String name, boolean alwaysEnabled, ResourcePackProfile.ResourcePackFactory packFactory, Text displayName,
			ResourcePackProfile.Info info, ResourcePackCompatibility compatibility, ResourcePackProfile.InsertionPosition position, boolean pinned,
			ResourcePackSource source,
			CallbackInfo ci) {
		try (var pack = packFactory.open(name)) {
			this.quilt$activationType = pack.getActivationType();
		}
	}

	@Override
	public @NotNull ResourcePackActivationType getActivationType() {
		return this.quilt$activationType;
	}
}
