package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import net.minecraft.util.Identifier;
import java.util.HashMap;

public final class BuiltinRegistryItemAttributeHolder<R> extends RegistryItemAttributeHolderImpl<R> {
	private final HashMap<Identifier, RegistryItemAttribute<R, ?>> attributes;

	public BuiltinRegistryItemAttributeHolder() {
		super();
		attributes = new HashMap<>();
	}

	public <T> void registerAttribute(RegistryItemAttribute<R, T> attribute) {
		attributes.put(attribute.getId(), attribute);
	}

	public RegistryItemAttribute<R, ?> getAttribute(Identifier id) {
		return attributes.get(id);
	}
}
