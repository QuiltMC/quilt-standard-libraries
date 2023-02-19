package org.quiltmc.qsl.debug_renderers.api.client;

import net.minecraft.client.render.debug.DebugRenderer;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;
import org.quiltmc.qsl.debug_renderers.api.DebugFeature;

@FunctionalInterface
public interface DebugRendererRegistrationCallback extends ClientEventAwareListener {
	Event<DebugRendererRegistrationCallback> EVENT = Event.create(DebugRendererRegistrationCallback.class, callbacks -> registrar -> {
		for (var callback : callbacks) {
			callback.registerDebugRenderers(registrar);
		}
	});

	void registerDebugRenderers(DebugRendererRegistrationCallback.DebugRendererRegistrar registrar);

	@FunctionalInterface
	interface DebugRendererRegistrar {
		void register(DebugFeature feature, DebugRenderer.Renderer renderer);
	}
}
