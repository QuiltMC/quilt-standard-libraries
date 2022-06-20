package org.quiltmc.qsl.component.api.identifier;

import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.Component;

import java.util.Optional;

public record ComponentIdentifier<T extends Component>(Identifier id){

	@SuppressWarnings("unchecked")
	public Optional<T> cast(Component component) {
		try {
			return Optional.of((T)component);
		} catch (ClassCastException ignored) {
			return Optional.empty();
		}
	}
}
