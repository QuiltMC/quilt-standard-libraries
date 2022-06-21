package org.quiltmc.qsl.component.api.components;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.defaults.DefaultFloatComponent;


public interface FloatComponent extends Component, NbtComponent<NbtFloat> {
	static ComponentIdentifier<FloatComponent> create(Identifier id) {
		return ComponentsImpl.register(id, DefaultFloatComponent::new);
	}

	static ComponentIdentifier<FloatComponent> create(float initialValue, Identifier id) {
		return ComponentsImpl.register(id, () -> new DefaultFloatComponent(initialValue));
	}

	float get();

	void set(float value);

	@Override
	default NbtFloat write() {
		return NbtFloat.of(this.get());
	}

	@Override
	default void read(NbtFloat nbt) {
		this.set(nbt.floatValue());
	}

	@Override
	default byte nbtType() {
		return NbtElement.FLOAT_TYPE;
	}
}
