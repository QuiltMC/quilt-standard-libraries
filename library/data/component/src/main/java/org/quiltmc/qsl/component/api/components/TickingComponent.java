package org.quiltmc.qsl.component.api.components;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentProvider;

public interface TickingComponent extends Component {
	void tick(@NotNull ComponentProvider provider);
}
