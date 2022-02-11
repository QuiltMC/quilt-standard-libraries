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

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.class_5350;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;

import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(class_5350.class)
public class Class5350Mixin {
	@Inject(method = "method_40427", at = @At("RETURN"), cancellable = true)
	private void onGetResourceReloaders(CallbackInfoReturnable<List<ResourceReloader>> cir) {
		// Re-inject resource reloaders server-side.
		// It is currently unknown why ReloadableResourceManager#reload isn't called anymore.
		var list = new ArrayList<>(cir.getReturnValue());
		ResourceLoaderImpl.sort(ResourceType.SERVER_DATA, list);
		cir.setReturnValue(list);
	}
}
