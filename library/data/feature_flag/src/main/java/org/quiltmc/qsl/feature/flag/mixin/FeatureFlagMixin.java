package org.quiltmc.qsl.feature.flag.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.feature_flags.FeatureFlagGroup;

import org.quiltmc.qsl.feature.flag.impl.QuiltFeatureFlagExtensions;

@Mixin(FeatureFlag.class)
public class FeatureFlagMixin implements QuiltFeatureFlagExtensions {
	@Mutable
	@Shadow
	@Final
	long mask;
	@Unique
	private int quilt$additionalMaskIndex = -1;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void setAdditionalMask(FeatureFlagGroup group, int mask, CallbackInfo ci) {
		if (mask >= 64) {
			this.mask = 0L;
			this.quilt$additionalMaskIndex = mask - 64;
		}
	}

	@Override
	public int quilt$getAdditionalMaskIndex() {
		return this.quilt$additionalMaskIndex;
	}
}
