package org.quiltmc.qsl.rendering.entity_models.api;

import java.util.Map;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.client.render.animation.PartAnimation;

public class Interpolators {
	private static final Map<String, PartAnimation.Interpolator> INTERPOLATORS = new Object2ObjectLinkedOpenHashMap<>();
	private static final Map<PartAnimation.Interpolator, String> INVERSE_INTERPOLATORS = new Object2ObjectLinkedOpenHashMap<>();

	static {
		register("LINEAR", PartAnimation.Interpolators.LINEAR);
		register("SPLINE", PartAnimation.Interpolators.SPLINE);
	}

	public static void register(String name, PartAnimation.Interpolator interpolator) {
		if (INTERPOLATORS.containsKey(name)) {
			throw new IllegalArgumentException(name + " already used as name");
		} else if (INVERSE_INTERPOLATORS.containsKey(interpolator)) {
			throw new IllegalArgumentException("Interpolator already assigned to " + INVERSE_INTERPOLATORS.get(interpolator));
		}
		INTERPOLATORS.put(name, interpolator);
		INVERSE_INTERPOLATORS.put(interpolator, name);
	}

	public static Optional<PartAnimation.Interpolator> get(String name) {
		return Optional.ofNullable(INTERPOLATORS.get(name));
	}

	public static Optional<String> get(PartAnimation.Interpolator interpolator) {
		return Optional.ofNullable(INVERSE_INTERPOLATORS.get(interpolator));
	}
}
