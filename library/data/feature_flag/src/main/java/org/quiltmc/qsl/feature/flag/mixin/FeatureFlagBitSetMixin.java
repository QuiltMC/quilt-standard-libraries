package org.quiltmc.qsl.feature.flag.mixin;

import java.util.BitSet;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.feature_flags.FeatureFlagGroup;

import org.quiltmc.qsl.feature.flag.impl.QuiltFeatureFlagExtensions;

@Mixin(FeatureFlagBitSet.class)
public class FeatureFlagBitSetMixin {
	@Unique
	private final BitSet quilt$additionalFlags = new BitSet();

	@Inject(method = "isIn", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
	private void checkAdditionalFlags(FeatureFlagBitSet other, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			BitSet bitSet = (BitSet) this.quilt$additionalFlags.clone();
			bitSet.andNot(((FeatureFlagBitSetMixin) (Object) other).quilt$additionalFlags);
			if (bitSet.cardinality() != 0) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "hasFlag", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	private void checkAdditionalFlags(FeatureFlag flag, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			int bitIndex = ((QuiltFeatureFlagExtensions) flag).quilt$getAdditionalMaskIndex();
			if (bitIndex >= 0 && this.quilt$additionalFlags.get(bitIndex)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "ofCollection", at = @At(value = "RETURN", ordinal = 1))
	private static void ofCollectionWithAdditionalFlags(FeatureFlagGroup group, Collection<FeatureFlag> flags, CallbackInfoReturnable<FeatureFlagBitSet> cir) {
		FeatureFlagBitSet captured = cir.getReturnValue();
		for (var flag : flags) {
			int bitIndex = ((QuiltFeatureFlagExtensions) flag).quilt$getAdditionalMaskIndex();
			if (bitIndex >= 0) {
				((FeatureFlagBitSetMixin) (Object) captured).quilt$additionalFlags.set(bitIndex);
			}
		}
	}

	@Inject(method = "ofFlag", at = @At("RETURN"))
	private static void ofFlagWithExtendedMask(FeatureFlag flag, CallbackInfoReturnable<FeatureFlagBitSet> cir) {
		FeatureFlagBitSet captured = cir.getReturnValue();
		int bitIndex = ((QuiltFeatureFlagExtensions) flag).quilt$getAdditionalMaskIndex();
		if (bitIndex >= 0) {
			((FeatureFlagBitSetMixin) (Object) captured).quilt$additionalFlags.set(bitIndex);
		}
	}

	@Inject(method = "ofFlags", at = @At("RETURN"))
	private static void ofFlagsWithAdditionalFlags(FeatureFlag first, FeatureFlag[] others, CallbackInfoReturnable<FeatureFlagBitSet> cir) {
		FeatureFlagBitSet captured = cir.getReturnValue();

		int bitIndex = ((QuiltFeatureFlagExtensions) first).quilt$getAdditionalMaskIndex();
		if (bitIndex >= 0) {
			((FeatureFlagBitSetMixin) (Object) captured).quilt$additionalFlags.set(bitIndex);
		}

		for (var flag : others) {
			bitIndex = ((QuiltFeatureFlagExtensions) flag).quilt$getAdditionalMaskIndex();
			if (bitIndex >= 0) {
				((FeatureFlagBitSetMixin) (Object) captured).quilt$additionalFlags.set(bitIndex);
			}
		}
	}

	@Inject(method = "union", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
	private void unionAdditionalFlags(FeatureFlagBitSet other, CallbackInfoReturnable<FeatureFlagBitSet> cir) {
		var bitSet = cir.getReturnValue();

		((FeatureFlagBitSetMixin) (Object) bitSet).quilt$additionalFlags.or(this.quilt$additionalFlags);

		var otherAdditionalFlags = ((FeatureFlagBitSetMixin) (Object) other).quilt$additionalFlags;
		((FeatureFlagBitSetMixin) (Object) bitSet).quilt$additionalFlags.or(otherAdditionalFlags);

		cir.setReturnValue(bitSet);
	}

	@Inject(method = "equals", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	private void equalsAdditionalFlags(Object object, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			cir.setReturnValue(this.quilt$additionalFlags.equals(((FeatureFlagBitSetMixin) object).quilt$additionalFlags));
		}
	}

	@Inject(method = "hashCode", at = @At("RETURN"), cancellable = true)
	private void hashAdditionalFlags(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(31 * cir.getReturnValue() + this.quilt$additionalFlags.hashCode());
	}
}
