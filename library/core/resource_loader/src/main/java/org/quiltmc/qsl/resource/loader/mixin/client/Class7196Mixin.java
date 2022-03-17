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

package org.quiltmc.qsl.resource.loader.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.class_7196;
import net.minecraft.class_7237;
import net.minecraft.server.WorldStem;
import net.minecraft.world.SaveProperties;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@Mixin(class_7196.class)
public class Class7196Mixin {
	@Inject(method = "method_41900", at = @At("HEAD"))
	private void onStartDataPackLoad(class_7237.class_7238 arg, class_7237.class_6907<SaveProperties> arg2, CallbackInfoReturnable<WorldStem> cir) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@Inject(method = "method_41900", at = @At("RETURN"))
	private void onEndDataPackLoad(class_7237.class_7238 arg, class_7237.class_6907<SaveProperties> arg2, CallbackInfoReturnable<WorldStem> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, cir.getReturnValue().resourceManager(), null);
	}

	@ModifyArg(
			method = {"method_41895", "method_41899"},
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false),
			index = 1
	)
	private Throwable onFailedDataPackLoad(Throwable throwable) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, throwable);
		return throwable; // noop
	}
}
