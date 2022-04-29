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

import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;

// TODO - Add Javadocs
@Environment(EnvType.CLIENT)
@InjectedInterface(KeyBind.class)
public interface ChordedKeyBind {
	default KeyChord getBoundChord() {
		System.out.println("This should never happen");
		return null;
	}

	default void setBoundChord(KeyChord chord) { }

	// TODO - This is a temporary measure until CHASM comes. Replace it with a proper constructor or builder
	default KeyBind withChord(InputUtil.Key... keys) {
		return null;
	}
}
