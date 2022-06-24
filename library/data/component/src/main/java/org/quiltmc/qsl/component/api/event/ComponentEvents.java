package org.quiltmc.qsl.component.api.event;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;

public class ComponentEvents {

	public static final Event<Inject> INJECT = Event.create(Inject.class, listeners -> (provider, creator) -> {
		for (Inject listener : listeners) {
			listener.onInject(provider, creator);
		}
	});

	@FunctionalInterface
	public interface Inject {
		void onInject(ComponentProvider provider, InjectionCreator creator);
	}

	@FunctionalInterface
	public interface InjectionCreator {
		void inject(ComponentType<?> type);
	}
}
