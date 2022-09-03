package org.quiltmc.qsl.command.api;

import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

import java.util.function.Predicate;

/**
 * Callback for registering {@link EntitySelectorOptions entity selector options}.
 */
public interface EntitySelectorOptionRegistrationCallback extends EventAwareListener {
	/**
	 * Event invoked when entity selector options are to be registered.
	 */
	Event<EntitySelectorOptionRegistrationCallback> EVENT = Event.create(EntitySelectorOptionRegistrationCallback.class, callbacks -> (registrar) -> {
		for (var callback : callbacks) {
			callback.registerEntitySelectors(registrar);
		}
	});

	/**
	 * Called when entity selector options are to be registered.
	 * @param registrar the selector options registrar
	 */
	void registerEntitySelectors(EntitySelectorOptionRegistrar registrar);

	/**
	 * Handles registering entity selector options.
	 */
	interface EntitySelectorOptionRegistrar {
		/**
		 * Registers an entity selector option.
		 *
		 * @param id			the id to register the option under
		 * @param handler		the handler for the option
		 * @param condition		the condition under which the option is available
		 * @param description	a description of the option
		 */
		void register(Identifier id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description);
	}
}
