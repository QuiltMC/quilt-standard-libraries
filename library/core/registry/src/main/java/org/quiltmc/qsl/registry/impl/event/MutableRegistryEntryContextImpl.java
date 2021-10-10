package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.registry.api.event.RegistryEntryContext;

/**
 * The default implementation for {@link RegistryEntryContext}.
 *
 * <p>In order to minimize allocations during event invocation, especially during registry iteration, this class is
 * mutable. The api interface only allows accessing fields of the class, whereas modification methods are reserved for the
 * impl.
 *
 * @param <V> the type of the relevant {@link Registry}'s entries
 */
@ApiStatus.Internal
public class MutableRegistryEntryContextImpl<V> implements RegistryEntryContext<V> {
	private final Registry<V> registry;
	private V entry;
	private Identifier id;
	private int raw = -1;

	public MutableRegistryEntryContextImpl(Registry<V> registry) {
		this.registry = registry;
	}

	/**
	 * Changes the current entry information.
	 *
	 * <p>Raw ID is set to -1 to signify that it should be lazily looked up.
	 *
	 * @param id    the namespaced ID of the new entry
	 * @param entry the new entry's object
	 */
	public void set(Identifier id, V entry) {
		this.set(id, entry, -1);
	}

	/**
	 * Changes the current entry information.
	 *
	 * @param id    the namespaced ID of the new entry
	 * @param entry the new entry's object
	 * @param rawId the raw int ID of the new entry
	 */
	public void set(Identifier id, V entry, int rawId) {
		this.id = id;
		this.entry = entry;
		this.raw = rawId;
	}

	@Override
	public Registry<V> registry() {
		return registry;
	}

	@Override
	public V entry() {
		return entry;
	}

	@Override
	public Identifier id() {
		return id;
	}

	@Override
	public int rawId() {
		if (raw < 0) {
			raw = registry.getRawId(entry);
		}
		return raw;
	}
}
