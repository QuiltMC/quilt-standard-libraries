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

package org.quiltmc.qsl.datafixerupper.impl;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import org.jetbrains.annotations.*;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;

import org.quiltmc.loader.api.QuiltLoader;

@ApiStatus.Internal
public abstract class QuiltDataFixesInternals {
	public record DataFixerEntry(DataFixer dataFixer, int currentVersion) {}

	@Contract(pure = true)
	@Range(from = 0, to = Integer.MAX_VALUE)
	public static int getModDataVersion(@NotNull NbtCompound compound, @NotNull String modId) {
		return compound.getInt(modId + "_DataVersion");
	}

	private static QuiltDataFixesInternals instance;

	public static @NotNull QuiltDataFixesInternals get() {
		if (instance == null) {
			if (QuiltLoader.isModLoaded("databreaker")) {
				instance = new NopQuiltDataFixesInternals();
			} else {
				instance = new QuiltDataFixesInternalsImpl();
			}
		}

		return instance;
	}

	public abstract void registerFixer(@NotNull String modId,
			@Range(from = 0, to = Integer.MAX_VALUE) int currentVersion,
			@NotNull DataFixer dataFixer);

	public abstract @Nullable DataFixerEntry getFixerEntry(@NotNull String modId);

	@Contract(value = "-> new", pure = true)
	public abstract @NotNull Schema createBaseSchema();

	public abstract @NotNull NbtCompound updateWithAllFixers(@NotNull DataFixTypes dataFixTypes,
			@NotNull NbtCompound compound);

	@Contract("_ -> new")
	public abstract @NotNull NbtCompound addModDataVersions(@NotNull NbtCompound compound);

	public abstract void freeze();

	@Contract(pure = true)
	public abstract boolean isFrozen();
}
