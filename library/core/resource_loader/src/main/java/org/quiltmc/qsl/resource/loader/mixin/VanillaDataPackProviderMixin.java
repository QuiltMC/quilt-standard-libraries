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

package org.quiltmc.qsl.resource.loader.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.PackSource;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.VanillaDataPackProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.QuiltPackProfile;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(VanillaDataPackProvider.class)
public class VanillaDataPackProviderMixin {
	@Shadow
	@Final
	private static Identifier DATA_PACKS_DIR;

	@ModifyArg(
			method = "createBuiltinPackProfile(Lnet/minecraft/resource/pack/ResourcePack;)Lnet/minecraft/resource/pack/PackProfile;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/VanillaDataPackProvider;wrapToFactory(Lnet/minecraft/resource/pack/ResourcePack;)Lnet/minecraft/resource/pack/PackProfile$PackFactory;"
			),
			index = 0
	)
	private ResourcePack onPackGet(ResourcePack pack) {
		return ResourceLoaderImpl.buildMinecraftPack(ResourceType.SERVER_DATA, pack);
	}

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
		return QuiltPackProfile.wrapToFactory(ResourceLoaderImpl.buildVanillaBuiltinPack(factory.openPrimary(name), ResourceType.SERVER_DATA,
				"data/" + DATA_PACKS_DIR.getNamespace() + '/' + DATA_PACKS_DIR.getPath() + '/' + name
		));
	}
}
