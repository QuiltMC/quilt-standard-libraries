package org.quiltmc.qsl.command.api;

import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

import java.util.function.Predicate;

public interface EntitySelectorOptionRegistrationCallback extends EventAwareListener {
	Event<EntitySelectorOptionRegistrationCallback> EVENT = Event.create(EntitySelectorOptionRegistrationCallback.class, callbacks -> (registrar) -> {
		for (var callback : callbacks) {
			callback.registerEntitySelectors(registrar);
		}
	});

	void registerEntitySelectors(EntitySelectorOptionRegistrar registrar);

	interface EntitySelectorOptionRegistrar {
		void register(Identifier id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description);
	}
}
