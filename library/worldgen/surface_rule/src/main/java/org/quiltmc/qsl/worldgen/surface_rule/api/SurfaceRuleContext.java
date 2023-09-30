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

package org.quiltmc.qsl.worldgen.surface_rule.api;

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

/**
 * Represents a context about surface rules for ease of modification of them.
 *
 * @see SurfaceRuleEvents
 */
public interface SurfaceRuleContext {
	/**
	 * {@return the list of the current surface material rules present}
	 * <p>
	 * The list is mutable.
	 */
	@Contract(pure = true)
	@NotNull List<SurfaceRules.MaterialRule> materialRules();

	/**
	 * {@return the resource manager of the current set of loaded data-packs}
	 */
	@Contract(pure = true)
	@NotNull ResourceManager resourceManager();

	/**
	 * {@return the identifier of the chunk generator settings that have those surface rules}
	 */
	@Contract(pure = true)
	@NotNull Identifier identifier();

	/**
	 * Represents the Overworld-specific context.
	 */
	interface Overworld extends SurfaceRuleContext {
		/**
		 * {@return {@code true} if this overworld dimension should have a surface exposed to the sky, or {@code false} otherwise}
		 */
		@Contract(pure = true)
		boolean hasSurface();

		/**
		 * {@return {@code true} if this overworld dimension should have a bedrock roof, or {@code false} otherwise}
		 */
		@Contract(pure = true)
		boolean hasBedrockRoof();

		/**
		 * {@return {@code true} if this overworld dimension should have a bedrock floor, or {@code false} otherwise}
		 */
		@Contract(pure = true)
		boolean hasBedrockFloor();
	}

	/**
	 * Represents the Nether-specific context.
	 */
	interface Nether extends SurfaceRuleContext {
	}

	/**
	 * Represents the End-specific context.
	 */
	interface TheEnd extends SurfaceRuleContext {
	}
}
