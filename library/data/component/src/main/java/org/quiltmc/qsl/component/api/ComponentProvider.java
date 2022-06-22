package org.quiltmc.qsl.component.api;

import org.jetbrains.annotations.NotNull;

public interface ComponentProvider {
	@NotNull
	ComponentContainer getContainer();
}
