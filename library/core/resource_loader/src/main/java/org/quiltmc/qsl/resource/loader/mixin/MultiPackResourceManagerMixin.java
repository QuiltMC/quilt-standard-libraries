/*
 * Copyright 2021-2022 QuiltMC
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

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.pack.ResourcePack;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {
	@Inject(method = "streamResourcePacks", at = @At("RETURN"), cancellable = true)
	private void onStreamResourcePacks(CallbackInfoReturnable<Stream<ResourcePack>> cir) {
		cir.setReturnValue(cir.getReturnValue()
				.flatMap(pack -> {
					if (pack instanceof GroupResourcePack grouped) {
						return grouped.streamPacks();
					} else {
						return Stream.of(pack);
					}
				})
		);
	}
}
