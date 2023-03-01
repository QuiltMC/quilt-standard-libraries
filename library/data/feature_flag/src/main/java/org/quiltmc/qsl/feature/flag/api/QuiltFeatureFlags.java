/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.feature.flag.api;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.feature.flag.impl.QuiltFeatureFlagRegistryExtensions;

/**
 * Provides helper methods for working with {@link FeatureFlag}s.
 */
@ApiStatus.NonExtendable
public interface QuiltFeatureFlags {
	/**
	 * Registers the provided {@link Identifier} as a {@link FeatureFlag} in the {@link FeatureFlags#MAIN_REGISTRY}.
	 * @param id the {@link Identifier} for the {@link FeatureFlag}
	 * @return the created {@link FeatureFlag}
	 */
	static FeatureFlag registerFlag(Identifier id) {
		return ((QuiltFeatureFlagRegistryExtensions) FeatureFlags.MAIN_REGISTRY).quilt$registerFlag(id);
	}
}
