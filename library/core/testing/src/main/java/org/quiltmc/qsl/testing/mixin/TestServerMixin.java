/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.testing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.test.TestServer;

@Mixin(TestServer.class)
public class TestServerMixin {
	@Inject(method = "isDedicated", at = @At("HEAD"), cancellable = true)
	public void isDedicated(CallbackInfoReturnable<Boolean> cir) {
		// Allow dedicated server commands to be registered.
		// Should aid with mods that use this to detect if they are running on a dedicated server as well.
		cir.setReturnValue(true);
	}

	@ModifyConstant(
			method = "tick",
			constant = @Constant(stringValue = "All {} required tests passed :)")
	)
	private static String replaceSuccessMessage(String original) {
		// You may ask why, it's simple.
		// The original emoticon is a bit... weird.
		// And QSL members expressed some kind of interest into replacing it.
		// So here it is. I assure you this is a really necessary injection.
		return "All {} required tests passed :3c";
	}
}
