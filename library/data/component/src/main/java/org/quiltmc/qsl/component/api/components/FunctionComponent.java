package org.quiltmc.qsl.component.api.components;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentProvider;

@FunctionalInterface
public interface FunctionComponent<T, U> extends Component {
	U call(@NotNull ComponentProvider provider, T t);
}
