package org.quiltmc.qsl.component.api;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface ComponentProvider {

	Optional<Component> expose(ComponentIdentifier<?> id);

	ImmutableCollection<Component> exposeAll();

	static ImmutableMap<Identifier, Component> createComponents(ComponentProvider provider) {
		System.out.println(provider.getClass()); // TODO: Remove this
		var builder = ImmutableMap.<Identifier, Component>builder();

		Map<Identifier, Supplier<? extends Component>> injections = ComponentsImpl.get(provider);
		injections.forEach((id, supplier) -> builder.put(id, supplier.get()));

		return builder.build();
	}

}
