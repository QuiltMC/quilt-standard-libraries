package org.quiltmc.qsl.feature.flag.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.feature_flags.FeatureFlagGroup;

@Mixin(FeatureFlag.class)
public interface FeatureFlagAccessor {
	@Invoker("<init>")
	static FeatureFlag create(FeatureFlagGroup group, int index) {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
