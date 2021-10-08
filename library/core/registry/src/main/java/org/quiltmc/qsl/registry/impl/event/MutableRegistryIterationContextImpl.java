package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.api.event.RegistryIterationContext;

public class MutableRegistryIterationContextImpl<V> implements RegistryIterationContext<V> {
	private final Registry<V> registry;
	private V entry;
	private Identifier id;
	private int raw = -1;

	public MutableRegistryIterationContextImpl(Registry<V> registry) {
		this.registry = registry;
	}

	public void set(Identifier id, V entry) {
		this.set(id, entry, -1);
	}

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
