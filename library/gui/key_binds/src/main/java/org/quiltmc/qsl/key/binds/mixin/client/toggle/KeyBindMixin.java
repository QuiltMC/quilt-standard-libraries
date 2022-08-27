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

package org.quiltmc.qsl.key.binds.mixin.client.toggle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.InputUtil;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.key.binds.api.ToggleableKeyBind;
import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;

@Mixin(KeyBind.class)
public abstract class KeyBindMixin implements ToggleableKeyBind {
	@Shadow
	@Final
	private static Map<String, KeyBind> KEY_BINDS;

	@Unique
	private static final List<KeyBind> QUILT$ALL_KEY_BINDS = new ArrayList<>();

	@Unique
	private int quilt$disableCounter;

	@Shadow
	public abstract String getTranslationKey();

	@Shadow
	abstract void reset();

	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputUtil$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void initializeToggleFields(String string, InputUtil.Type type, int i, String string2, CallbackInfo ci) {
		for (KeyBind otherKey : QUILT$ALL_KEY_BINDS) {
			if (this.equals(otherKey)) {
				throw new IllegalArgumentException(String.format("%s has already been registered!", this.getTranslationKey()));
			} else if (this.getTranslationKey().equals(otherKey.getTranslationKey())) {
				throw new IllegalArgumentException(String.format("Attempted to register {}, but a key bind with the same translation key has already been registered!", this.getTranslationKey()));
			}
		}

		KeyBindRegistryImpl.registerKeyBind((KeyBind) (Object) this);
		this.quilt$disableCounter = 0;
	}

	@Override
	public boolean isEnabled() {
		return this.quilt$disableCounter == 0;
	}

	@Override
	public boolean isDisabled() {
		return this.quilt$disableCounter > 0;
	}

	@Override
	public boolean canDisable() {
		return true;
	}

	@Override
	public void enable() {
		// Hahahahaha no.
		if (this.quilt$disableCounter <= 0) return;

		this.quilt$disableCounter--;
		if (this.quilt$disableCounter == 0) {
			KeyBindRegistryImpl.updateKeyBindState((KeyBind) (Object) this);
			KEY_BINDS.put(this.getTranslationKey(), (KeyBind) (Object) this);
			KeyBind.updateBoundKeys();
			this.reset();
		}
	}

	@Override
	public void disable() {
		this.quilt$disableCounter++;
		if (this.quilt$disableCounter == 1) {
			KeyBindRegistryImpl.updateKeyBindState((KeyBind) (Object) this);
			KEY_BINDS.remove(this.getTranslationKey(), (KeyBind) (Object) this);
			KeyBind.updateBoundKeys();
			this.reset();
		}
	}
}
