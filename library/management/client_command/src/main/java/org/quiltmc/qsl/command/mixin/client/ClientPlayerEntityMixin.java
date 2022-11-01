/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.command.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import org.quiltmc.qsl.command.impl.client.ClientCommandInternals;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin {
	@Inject(method = "m_rkzxvlsn", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, CallbackInfoReturnable<Boolean> cir) {
		if (ClientCommandInternals.executeCommand(command, true)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "m_bcypipmz", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, Text preview, CallbackInfo ci) {
		if (ClientCommandInternals.executeCommand(command, true)) {
			ci.cancel();
		}
	}
}
