/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.biome.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.worldgen.biome.impl.modification.BiomeModificationImpl;

/**
 * @see BiomeModifications
 */
public class BiomeModification {
	private final Identifier id;

	@ApiStatus.Internal
	BiomeModification(Identifier id) {
		this.id = id;
	}

	/**
	 * Adds a modifier that is not sensitive to the current state of the biome when it is applied, examples
	 * for this are modifiers that simply add or remove features unconditionally, or change other values
	 * to constants.
	 */
	public BiomeModification add(ModificationPhase phase, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
		BiomeModificationImpl.INSTANCE.addModifier(this.id, phase, selector, modifier);
		return this;
	}

	/**
	 * Adds a modifier that is sensitive to the current state of the biome when it is applied.
	 * Examples for this are modifiers that apply scales to existing values (e.g. half the temperature).
	 * <p>
	 * For modifiers that should only be applied if a given condition is met for a Biome, please add these
	 * conditions to the selector, and use a context-free modifier instead, as this will greatly help
	 * with debugging world generation issues.
	 */
	public BiomeModification add(ModificationPhase phase, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
		BiomeModificationImpl.INSTANCE.addModifier(this.id, phase, selector, modifier);
		return this;
	}

	/**
	 * Adds a modifier that can be overridden by datapacks. Note that only a single such modifier can be added with any
	 * given identifier, and subsequent calls to this method will override the previous one.
	 */
	public BiomeModification add(ModificationPhase phase, BiomeModifier modifier) {
		BiomeModificationImpl.INSTANCE.addModifier(this.id, phase, modifier);
		return this;
	}
}
