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

package org.quiltmc.qsl.key.binds.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.mojang.blaze3d.platform.InputUtil;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;

// TODO - Explain what are key chords.
/**
 * An interface that adds key chord support to key binds.
 */
@Environment(EnvType.CLIENT)
@InjectedInterface(KeyBind.class)
public interface ChordedKeyBind {
	/**
	 * Gets the bound key chord of the key bind.
	 *
	 * @return the key bind's bound key chord.
	 */
	default KeyChord getBoundChord() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the bound key chord of the key bind.
	 */
	default void setBoundChord(KeyChord chord) { }

	// TODO - This is a temporary measure until Chasm arrives. Replace it with a proper constructor or builder
	/**
	 * Specifies the default key chord for the key bind.
	 *
	 * <p>This method is to be used only on creating a key bind instance.
	 *
	 * @param keys the keys of the default key chord
	 * @return the original key bind instance
	 */
	default KeyBind withChord(InputUtil.Key... keys) {
		throw new UnsupportedOperationException();
	}
}
