package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Represents information about a registry entry.
 *
 * @param <V> The entry type used by the relevant {@link Registry}.
 */
public interface RegistryEntryContext<V> {
	/**
	 * Getter for the relevant registry.
	 *
	 * @return The relevant registry for this entry.
	 */
	Registry<V> registry();

	/**
	 * Getter for the entry object.
	 *
	 * @return The entry's object.
	 */
	V entry();

	/**
	 * Getter for the namespaced identifier associated with the entry.
	 *
	 * @return The entry's namespaced identifier.
	 */
	Identifier id();

	/**
	 * Getter for the raw int ID associated with the entry.
	 *
	 * @return The entry's raw int ID.
	 */
	int rawId();
}
