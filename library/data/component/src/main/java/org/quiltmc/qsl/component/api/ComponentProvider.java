package org.quiltmc.qsl.component.api;

import com.google.common.collect.ImmutableCollection;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;

import java.util.Optional;

public interface ComponentProvider {

	Optional<Component> expose(ComponentIdentifier<?> id);

	ImmutableCollection<Component> exposeAll();

}
