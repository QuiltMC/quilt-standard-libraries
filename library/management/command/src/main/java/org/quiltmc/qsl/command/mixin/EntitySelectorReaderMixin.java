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

package org.quiltmc.qsl.command.mixin;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.command.EntitySelectorReader;

import org.quiltmc.qsl.command.api.QuiltEntitySelectorReader;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin implements QuiltEntitySelectorReader {
	@Unique
	private final Set<String> quilt$flags = new HashSet<>();

	@Override
	public boolean getFlag(@NotNull String key) {
		return this.quilt$flags.contains(key);
	}

	@Override
	public void setFlag(@NotNull String key, boolean value) {
		if (value) {
			this.quilt$flags.add(key);
		} else {
			this.quilt$flags.remove(key);
		}
	}
}
