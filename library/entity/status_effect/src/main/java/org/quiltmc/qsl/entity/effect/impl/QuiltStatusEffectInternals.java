package org.quiltmc.qsl.entity.effect.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

@ApiStatus.Internal
public final class QuiltStatusEffectInternals {
	private QuiltStatusEffectInternals() {
		throw new UnsupportedOperationException("QuiltStatusEffectInternals only contains static definitions.");
	}

	public static final String NAMESPACE = "quilt_status_effect";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	public static final StatusEffectRemovalReason UNKNOWN_REASON = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("unknown"));
}
