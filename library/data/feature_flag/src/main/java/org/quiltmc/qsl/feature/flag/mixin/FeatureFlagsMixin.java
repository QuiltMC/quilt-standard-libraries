package org.quiltmc.qsl.feature.flag.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.feature_flags.FeatureFlagRegistry;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.util.Identifier;

@Mixin(FeatureFlags.class)
public class FeatureFlagsMixin {
	@ModifyVariable(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/feature_flags/FeatureFlagRegistry$Builder;build()Lnet/minecraft/feature_flags/FeatureFlagRegistry;"))
	private static FeatureFlagRegistry.Builder addModdedFlag(FeatureFlagRegistry.Builder builder) {
		builder.createFlag(new Identifier("quilt", "modded"));
		return builder;
	}
}
