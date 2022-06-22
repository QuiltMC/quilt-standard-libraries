package org.quiltmc.qsl.component.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.util.Lazy;

import java.util.HashMap;
import java.util.Map;

public interface ComponentProvider {

	static @NotNull Map<Identifier, Lazy<Component>> createComponents(@NotNull ComponentProvider provider) {
		// System.out.println(provider.getClass()); // TODO: Remove this
		var map = new HashMap<Identifier, Lazy<Component>>();
		ComponentsImpl.get(provider).forEach((identifier, supplier) -> map.put(identifier, Lazy.of(supplier::get)));

		return map;
	}

	@NotNull
	ComponentContainer getContainer();

}
