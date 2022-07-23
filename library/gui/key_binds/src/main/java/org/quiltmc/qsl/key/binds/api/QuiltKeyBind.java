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

public interface QuiltKeyBind {
	/**
	 * Gets whenever the key bind is from Vanilla or not.
	 * This is automatically determined by using GameOptions' allKeys property.
	 *
	 * @return {@code true} if the key bind is from Vanilla, or {@code false} otherwise
	 */
	default boolean isVanilla() {
		return false;
	}

	/**
	 * Gets the bound key of the key bind.
	 *
	 * <p>The bound key is only directly used by the key bind system's internal logic.
	 * If possible, use the methods provided by the KeyBind class instead.
	 *
	 * @return the key bind's bound key
	 */
	default InputUtil.Key getBoundKey() {
		throw new UnsupportedOperationException();
	}
}
