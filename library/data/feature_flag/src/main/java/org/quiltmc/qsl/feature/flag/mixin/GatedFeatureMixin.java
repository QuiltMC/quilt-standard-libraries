package org.quiltmc.qsl.feature.flag.mixin;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.feature_flags.GatedFeature;

@Mixin(GatedFeature.class)
public interface GatedFeatureMixin {
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Set;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Set;"))
	private static Set<?> makeFilteredRegistriesMutable(Object e1, Object e2, Object e3) {
		return new HashSet<>(Set.of(e1, e2, e3));
	}
}
