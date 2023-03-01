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
