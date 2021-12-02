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

package org.quiltmc.qsl.key.bindings.mixin.client;

import java.io.File;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.quiltmc.qsl.key.bindings.impl.KeyBindingRegistryImpl;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
	@Shadow
	@Final
	public KeyBinding[] keysAll;

	@Inject(
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/option/GameOptions;load()V"
			),
			method = "<init>"
	)
	private void modifyAllKeys(MinecraftClient client, File file, CallbackInfo ci) {
		KeyBindingRegistryImpl.registerKeyBindingManager((GameOptions) (Object) this, keysAll);
	}

	@Inject(
			at = @At(
				value = "FIELD",
				target = "Lnet/minecraft/client/option/GameOptions;keysAll:[Lnet/minecraft/client/option/KeyBinding;"
			),
			method = "accept(Lnet/minecraft/client/option/GameOptions$Visitor;)V"
	)
	private void addDisabledEntries(GameOptions.Visitor visitor, CallbackInfo ci) {
		for (KeyBinding keyBinding : KeyBindingRegistryImpl.getDisabledKeyBindings()) {
			String keyKey = keyBinding.getBoundKeyTranslationKey();
			String keyBindKey = visitor.visitString("key_" + keyBinding.getTranslationKey(), keyKey);
			if (!keyKey.equals(keyBindKey)) {
				keyBinding.setBoundKey(InputUtil.fromTranslationKey(keyKey));
			}
		};
	}
}
