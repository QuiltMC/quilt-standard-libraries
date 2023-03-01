package org.quiltmc.qsl.feature.flag.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.feature_flags.FeatureFlagRegistry;

@Mixin(FeatureFlagRegistry.Builder.class)
public class FeatureFlagRegistryBuilderMixin {
	@ModifyConstant(method = "createFlag", constant = @Constant(intValue = 64))
	private int expandFlagLimit(int oldLimit) {
		return Integer.MAX_VALUE;
	}
}
