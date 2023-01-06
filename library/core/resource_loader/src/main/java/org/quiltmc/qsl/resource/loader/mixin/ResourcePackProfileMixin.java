/*
 * Copyright 2022-2023 QuiltMC
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

import java.util.Objects;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.metadata.PackResourceMetadata;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.resource.pack.ResourcePack;
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

	@Inject(
			method = "<init>(Ljava/lang/String;ZLnet/minecraft/resource/pack/ResourcePackProfile$ResourcePackFactory;Lnet/minecraft/text/Text;Lnet/minecraft/resource/pack/ResourcePackProfile$Info;Lnet/minecraft/resource/pack/ResourcePackCompatibility;Lnet/minecraft/resource/pack/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/pack/ResourcePackSource;)V",
			at = @At("RETURN")
	)
	private void quilt$onInit(String name, boolean alwaysEnabled, ResourcePackProfile.ResourcePackFactory packFactory, Text displayName,
			ResourcePackProfile.Info info, ResourcePackCompatibility compatibility, ResourcePackProfile.InsertionPosition position, boolean pinned,
			ResourcePackSource source, CallbackInfo ci) {
		var sourceActivationType = source.shouldAddAutomatically() ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL;
		this.quilt$activationType = alwaysEnabled ? ResourcePackActivationType.ALWAYS_ENABLED : sourceActivationType;
	}

	@Override
	public @NotNull ResourcePackActivationType getActivationType() {
		return this.quilt$activationType;
	}
}
