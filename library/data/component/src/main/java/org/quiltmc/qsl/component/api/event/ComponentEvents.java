package org.quiltmc.qsl.component.api.event;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.components.IntegerComponent;

import java.util.function.BooleanSupplier;

public class ComponentEvents {

	public static final Event<DynamicInject> DYNAMIC_INJECT = Event.create(DynamicInject.class, listeners -> (provider, injector) -> {
		for (DynamicInject listener : listeners) {
			listener.onInject(provider, injector);
		}
	});

	@FunctionalInterface
	public interface DynamicInject {
		void onInject(ComponentProvider provider, Injector injector);
	}

	@FunctionalInterface
	public interface Injector {
		void inject(ComponentType<?> type);

		default void injectIf(boolean condition, ComponentType<?> type) {
			if (condition) {
				this.inject(type);
			}
		}
	}
}
