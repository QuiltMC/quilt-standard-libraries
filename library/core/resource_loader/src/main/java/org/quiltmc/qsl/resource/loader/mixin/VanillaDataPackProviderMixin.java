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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.resource.PackPosition;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackLocationInfo;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.VanillaDataPackProvider;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(VanillaDataPackProvider.class)
public class VanillaDataPackProviderMixin {
	@Shadow
	@Final
	private static Identifier DATA_PACKS_DIR;

	@WrapOperation(
			method = "createVanillaPackProfile",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/resource/pack/PackProfile;of(Lnet/minecraft/resource/pack/PackLocationInfo;Lnet/minecraft/resource/pack/PackProfile$PackFactory;Lnet/minecraft/resource/ResourceType;Lnet/minecraft/resource/PackPosition;)Lnet/minecraft/resource/pack/PackProfile;"
			)
	)
	private PackProfile onCreateVanillaPackProfile(PackLocationInfo locationInfo, PackProfile.PackFactory packFactory, ResourceType type, PackPosition position, Operation<PackProfile> original) {
		packFactory = ResourceLoaderImpl
			.buildMinecraftPack(
				ResourceType.SERVER_DATA,
				packFactory.openPrimary(locationInfo))
			.wrapToFactory();

		return original.call(locationInfo, packFactory, type, position);
	}

	@WrapOperation(
			method = "createBuiltinPackProfile(Ljava/lang/String;Lnet/minecraft/resource/pack/PackProfile$PackFactory;Lnet/minecraft/text/Text;)Lnet/minecraft/resource/pack/PackProfile;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/PackProfile;of(Lnet/minecraft/resource/pack/PackLocationInfo;Lnet/minecraft/resource/pack/PackProfile$PackFactory;Lnet/minecraft/resource/ResourceType;Lnet/minecraft/resource/PackPosition;)Lnet/minecraft/resource/pack/PackProfile;"
			)
	)
	private PackProfile onCreateBuiltinPackProfile(PackLocationInfo locationInfo, PackProfile.PackFactory packFactory, ResourceType type, PackPosition position, Operation<PackProfile> original) {
		packFactory = ResourceLoaderImpl.buildVanillaBuiltinPack(
			packFactory.openPrimary(locationInfo),
			ResourceType.SERVER_DATA,
			"data/" + DATA_PACKS_DIR.getNamespace() + '/' + DATA_PACKS_DIR.getPath() + '/' + locationInfo.id()
		).wrapToFactory();

		return original.call(locationInfo, packFactory, type, position);
	}
}
