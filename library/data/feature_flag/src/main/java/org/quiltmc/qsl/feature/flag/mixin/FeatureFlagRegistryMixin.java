package org.quiltmc.qsl.feature.flag.mixin;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.feature_flags.FeatureFlagGroup;
import net.minecraft.feature_flags.FeatureFlagRegistry;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.feature.flag.impl.QuiltFeatureFlagRegistryExtensions;

@Mixin(FeatureFlagRegistry.class)
public class FeatureFlagRegistryMixin implements QuiltFeatureFlagRegistryExtensions {
	@Mutable
	@Shadow
	@Final
	private Map<Identifier, FeatureFlag> flags;

	@Mutable
	@Shadow
	@Final
	private FeatureFlagBitSet all;

	@Shadow
	@Final
	private FeatureFlagGroup group;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void makeFlagsMutable(FeatureFlagGroup group, FeatureFlagBitSet all, Map<Identifier, FeatureFlag> flags, CallbackInfo ci) {
		this.flags = new HashMap<>(flags);
	}

	@Unique
	@Override
	public @Nullable FeatureFlag quilt$registerFlag(Identifier id) {
		FeatureFlag flag = FeatureFlagAccessor.create(this.group, this.flags.size());
		var replacedFlag = this.flags.put(id, flag);
		if (replacedFlag != null) {
			throw new IllegalStateException("Duplicate feature flag " + id);
		}

		this.all = this.all.union(FeatureFlagBitSet.ofFlag(flag));
		return flag;
	}
}
