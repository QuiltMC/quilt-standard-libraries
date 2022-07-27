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

import java.util.function.Supplier;

import com.mojang.serialization.JsonOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.test.TestServer;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryOps;

import org.quiltmc.qsl.resource.loader.impl.ModResourcePackUtil;

@Mixin(TestServer.class)
public class TestServerMixin {
	@ModifyArg(
			method = "create",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/unmapped/C_kjxfcecs$C_nrmvgbka;<init>(Lnet/minecraft/resource/pack/ResourcePackManager;Lnet/minecraft/resource/pack/DataPackSettings;Z)V"
			),
			index = 1
	)
	private static DataPackSettings replaceDefaultDataPackSettings(DataPackSettings initialDataPacks) {
		return ModResourcePackUtil.DEFAULT_SETTINGS;
	}

	@Redirect(
			method = {"method_40377", "m_pckcekot"},
			at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;", remap = false),
			require = 1,
			remap = false
	)
	private static Object loadRegistry(Supplier<Object> unused, ResourceManager resourceManager) {
		DynamicRegistryManager.Writable registryManager = DynamicRegistryManager.builtInCopy();
		// Force-loads the dynamic registry from data-packs as some mods may define dynamic game objects via data-driven capabilities.
		RegistryOps.createAndLoad(JsonOps.INSTANCE, registryManager, resourceManager);
		return registryManager.freeze();
	}
}
