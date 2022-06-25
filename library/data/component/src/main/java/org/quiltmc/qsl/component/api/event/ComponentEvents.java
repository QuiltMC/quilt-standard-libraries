package org.quiltmc.qsl.component.api.event;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;

public class ComponentEvents {

	public static final Event<Inject> INJECT = Event.create(Inject.class, listeners -> (provider, injector) -> {
		for (Inject listener : listeners) {
			listener.onInject(provider, injector);
		}
	});

	@FunctionalInterface
	public interface Inject {
		void onInject(ComponentProvider provider, Injector injector);
	}

	@FunctionalInterface
	public interface Injector {
		void inject(ComponentType<?> type);
	}
}
