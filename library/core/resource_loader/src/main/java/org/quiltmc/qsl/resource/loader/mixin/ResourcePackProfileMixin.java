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

package org.quiltmc.qsl.resource.loader.mixin;

import java.util.Objects;
import java.util.function.Supplier;

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
	private static final ThreadLocal<ResourcePackActivationType> quilt$KNOWN_ACTIVATION_TYPE = new ThreadLocal<>();
	@Unique
	private ResourcePackActivationType quilt$activationType;

	@Inject(
			method = "of",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/ResourcePackProfile$Factory;create(Ljava/lang/String;Lnet/minecraft/text/Text;ZLjava/util/function/Supplier;Lnet/minecraft/resource/pack/metadata/PackResourceMetadata;Lnet/minecraft/resource/pack/ResourcePackProfile$InsertionPosition;Lnet/minecraft/resource/pack/ResourcePackSource;)Lnet/minecraft/resource/pack/ResourcePackProfile;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void onProfileCreation(String name, boolean alwaysEnabled, Supplier<ResourcePack> packFactory,
			ResourcePackProfile.Factory profileFactory, ResourcePackProfile.InsertionPosition insertionPosition, ResourcePackSource packSource,
			CallbackInfoReturnable<ResourcePackProfile> cir, ResourcePack pack) {
		quilt$KNOWN_ACTIVATION_TYPE.set(alwaysEnabled ? ResourcePackActivationType.ALWAYS_ENABLED : pack.getActivationType());
	}

	@Inject(
			method = "<init>(Ljava/lang/String;ZLjava/util/function/Supplier;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/resource/pack/ResourcePackCompatibility;Lnet/minecraft/resource/pack/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/pack/ResourcePackSource;)V",
			at = @At("RETURN")
	)
	private void onInit(String name, boolean alwaysEnabled, Supplier<ResourcePack> packFactory, Text displayName, Text description,
			ResourcePackCompatibility compatibility, ResourcePackProfile.InsertionPosition direction, boolean pinned, ResourcePackSource source,
			CallbackInfo ci) {
		var activationType = quilt$KNOWN_ACTIVATION_TYPE.get();
		quilt$KNOWN_ACTIVATION_TYPE.remove();

		this.quilt$activationType = Objects.requireNonNullElse(activationType,
				alwaysEnabled ? ResourcePackActivationType.ALWAYS_ENABLED : ResourcePackActivationType.NORMAL
		);
	}

	@Override
	public @NotNull ResourcePackActivationType getActivationType() {
		return this.quilt$activationType;
	}
}
