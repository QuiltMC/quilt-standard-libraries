package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import net.minecraft.util.Identifier;
import java.util.HashMap;

public final class BuiltinRegistryEntryAttributeHolder<R> extends RegistryEntryAttributeHolderImpl<R> {
	private final HashMap<Identifier, RegistryEntryAttribute<R, ?>> attributes;

	public BuiltinRegistryEntryAttributeHolder() {
		super();
		attributes = new HashMap<>();
	}

	public <T> void registerAttribute(RegistryEntryAttribute<R, T> attribute) {
		attributes.put(attribute.getId(), attribute);
	}

	public RegistryEntryAttribute<R, ?> getAttribute(Identifier id) {
		return attributes.get(id);
	}
}
