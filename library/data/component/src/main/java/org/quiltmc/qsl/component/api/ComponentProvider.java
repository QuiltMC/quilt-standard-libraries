package org.quiltmc.qsl.component.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface ComponentProvider {

	static @NotNull Map<Identifier, Component> createComponents(@NotNull ComponentProvider provider) {
		// System.out.println(provider.getClass()); // TODO: Remove this
		var map = new HashMap<Identifier, Component>();
		ComponentsImpl.get(provider).forEach((identifier, supplier) -> map.put(identifier, supplier.get()));

		return map;
	}

	Optional<Component> expose(ComponentIdentifier<?> id);

	Map<Identifier, Component> exposeAll();

}
