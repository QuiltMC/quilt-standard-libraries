package org.quiltmc.qsl.feature.flag.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public interface QuiltFeatureFlagRegistryExtensions {
	@Nullable FeatureFlag quilt$registerFlag(Identifier id);
}
