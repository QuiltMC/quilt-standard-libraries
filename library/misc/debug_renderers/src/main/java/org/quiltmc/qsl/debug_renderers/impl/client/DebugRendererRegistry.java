package org.quiltmc.qsl.debug_renderers.impl.client;

import net.minecraft.client.render.debug.DebugRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.debug_renderers.api.DebugFeature;
import org.quiltmc.qsl.debug_renderers.impl.DebugFeaturesImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApiStatus.Internal
@ClientOnly
public final class DebugRendererRegistry {
	private static final Map<DebugFeature, DebugRenderer.Renderer> RENDERERS = new HashMap<>();

	DebugRendererRegistry() {}

	public static void register(DebugFeature feature, DebugRenderer.Renderer renderer) {
		RENDERERS.put(feature, renderer);
	}

	public static Collection<DebugRenderer.Renderer> getEnabledRenderers() {
		return DebugFeaturesImpl.getEnabledFeatures().stream()
				.filter(DebugFeature::shouldRender)
				.map(RENDERERS::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
