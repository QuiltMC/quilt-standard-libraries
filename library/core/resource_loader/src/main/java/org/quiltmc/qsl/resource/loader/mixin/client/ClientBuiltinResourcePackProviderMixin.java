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

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.BuiltinPackProvider;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.PackSource;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.QuiltPackProfile;
import org.quiltmc.qsl.resource.loader.impl.ModPackProvider;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@ClientOnly
@Mixin(ClientBuiltinResourcePackProvider.class)
public abstract class ClientBuiltinResourcePackProviderMixin {
	@Shadow
	@Final
	private static Map<String, Text> BUILTIN_PACK_DISPLAY_NAMES;

	@ModifyArg(
			method = "createBuiltinPackProfile(Ljava/lang/String;Lnet/minecraft/resource/pack/PackProfile$PackFactory;Lnet/minecraft/text/Text;)Lnet/minecraft/resource/pack/PackProfile;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/PackProfile;of(Ljava/lang/String;Lnet/minecraft/text/Text;ZLnet/minecraft/resource/pack/PackProfile$PackFactory;Lnet/minecraft/resource/ResourceType;Lnet/minecraft/resource/pack/PackProfile$InsertionPosition;Lnet/minecraft/resource/pack/PackSource;)Lnet/minecraft/resource/pack/PackProfile;"
			),
			index = 3
	)
	private PackProfile.PackFactory onCreateBuiltinResourcePackProfile(String name, Text displayName, boolean alwaysEnabled,
			PackProfile.PackFactory factory, ResourceType type, PackProfile.InsertionPosition insertionPosition,
			PackSource source) {
		if (BUILTIN_PACK_DISPLAY_NAMES.containsKey(name)) {
			return QuiltPackProfile.wrapToFactory(ResourceLoaderImpl.buildVanillaBuiltinPack(factory.openPrimary(name), ResourceType.CLIENT_RESOURCES, name));
		}

		return factory;
	}

	@ModifyArg(
			method = "createBuiltinPackProfile(Lnet/minecraft/resource/pack/ResourcePack;)Lnet/minecraft/resource/pack/PackProfile;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/resource/ClientBuiltinResourcePackProvider;wrapToFactory(Lnet/minecraft/resource/pack/ResourcePack;)Lnet/minecraft/resource/pack/PackProfile$PackFactory;"
			),
			index = 0
	)
	private ResourcePack onPackGet(ResourcePack pack) {
		return ResourceLoaderImpl.buildMinecraftPack(ResourceType.CLIENT_RESOURCES, pack);
	}

	@ClientOnly
	@Mixin(BuiltinPackProvider.class)
	public static class Parent {
		@SuppressWarnings("ConstantConditions")
		@Inject(method = "registerAdditionalPacks", at = @At("RETURN"))
		private void addBuiltinResourcePacks(Consumer<PackProfile> profileAdder, CallbackInfo ci) {
			// Register built-in resource packs after vanilla built-in resource packs are registered.
			if (((Object) this) instanceof ClientBuiltinResourcePackProvider) {
				ModPackProvider.CLIENT_RESOURCE_PACK_PROVIDER.register(profileAdder);
			}
		}
	}
}
