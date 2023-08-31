/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.mixin.client;

import java.util.Map;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.BuiltinResourcePackProvider;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.ResourcePackSource;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackProvider;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@ClientOnly
@Mixin(ClientBuiltinResourcePackProvider.class)
public class ClientBuiltinResourcePackProviderMixin {
	@Shadow
	@Final
	private static Map<String, Text> BUILTIN_PACK_DISPLAY_NAMES;

	@ModifyArg(
			method = "createBuiltinResourcePackProfile(Ljava/lang/String;Lnet/minecraft/resource/pack/ResourcePackProfile$ResourcePackFactory;Lnet/minecraft/text/Text;)Lnet/minecraft/resource/pack/ResourcePackProfile;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/ResourcePackProfile;of(Ljava/lang/String;Lnet/minecraft/text/Text;ZLnet/minecraft/resource/pack/ResourcePackProfile$ResourcePackFactory;Lnet/minecraft/resource/ResourceType;Lnet/minecraft/resource/pack/ResourcePackProfile$InsertionPosition;Lnet/minecraft/resource/pack/ResourcePackSource;)Lnet/minecraft/resource/pack/ResourcePackProfile;"
			),
			index = 3
	)
	private ResourcePackProfile.ResourcePackFactory onCreateBuiltinResourcePackProfile(String name, Text displayName, boolean alwaysEnabled,
			ResourcePackProfile.ResourcePackFactory factory, ResourceType type, ResourcePackProfile.InsertionPosition insertionPosition,
			ResourcePackSource source) {
		if (BUILTIN_PACK_DISPLAY_NAMES.containsKey(name)) {
			return n -> ResourceLoaderImpl.buildVanillaBuiltinResourcePack(factory.open(n), ResourceType.CLIENT_RESOURCES, name);
		}

		return factory;
	}

	// Synthetic method createBuiltinResourcePackProfile(ResourcePack)ResourcePackProfile
	// Using an injector to wrap the previous return value.
	@Inject(
			method = "method_45855(Lnet/minecraft/resource/pack/ResourcePack;Ljava/lang/String;)Lnet/minecraft/resource/pack/ResourcePack;",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void onPackGet(ResourcePack pack, String name, CallbackInfoReturnable<ResourcePack> cir) {
		cir.setReturnValue(ResourceLoaderImpl.buildMinecraftResourcePack(ResourceType.CLIENT_RESOURCES, cir.getReturnValue()));
	}

	@ClientOnly
	@Mixin(BuiltinResourcePackProvider.class)
	public static class Parent {
		@SuppressWarnings("ConstantConditions")
		@Inject(method = "registerAdditionalPacks", at = @At("RETURN"))
		private void addBuiltinResourcePacks(Consumer<ResourcePackProfile> profileAdder, CallbackInfo ci) {
			// Register built-in resource packs after vanilla built-in resource packs are registered.
			if (((Object) this) instanceof ClientBuiltinResourcePackProvider) {
				ModResourcePackProvider.CLIENT_RESOURCE_PACK_PROVIDER.register(profileAdder);
			}
		}
	}
}
