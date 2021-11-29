/*
 * Copyright 2021 QuiltMC
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

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;

import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackProvider;

@Environment(EnvType.CLIENT)
@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
	@ModifyArg(
			method = "create",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/resource/DataPackSettings;Lnet/minecraft/client/gui/screen/world/MoreOptionsDialog;)V"
			),
			index = 1
	)
	private static DataPackSettings onNew(DataPackSettings settings) {
		var moddedResourcePacks = new ArrayList<ResourcePackProfile>();
		ModResourcePackProvider.SERVER_RESOURCE_PACK_PROVIDER.register(moddedResourcePacks::add);

		var enabled = new ArrayList<>(settings.getEnabled());
		var disabled = new ArrayList<>(settings.getDisabled());

		// This ensure that any built-in registered data packs by mods which needs to be enabled by default are
		// as the data pack screen automatically put any data pack as disabled except the Default data pack.
		for (var profile : moddedResourcePacks) {
			ResourcePack pack = profile.createResourcePack();

			if (pack instanceof ModNioResourcePack modResourcePack && modResourcePack.getActivationType().isEnabledByDefault()) {
				enabled.add(profile.getName());
			} else {
				disabled.add(profile.getName());
			}
		}

		return new DataPackSettings(enabled, disabled);
	}
}
