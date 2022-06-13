package org.quiltmc.qsl.component.api.components;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.defaults.DefaultIntegerComponent;

import java.util.function.Supplier;

public interface IntegerComponent extends NbtComponent<NbtInt> {
	static ComponentIdentifier<IntegerComponent> create(int initialValue, Identifier id) {
		Supplier<IntegerComponent> supplier = () -> new DefaultIntegerComponent(initialValue);
		return ComponentsImpl.register(id, supplier);
	}

	static ComponentIdentifier<IntegerComponent> create(Identifier id) {
		return ComponentsImpl.register(id, DefaultIntegerComponent::new);
	}

	int get();

	void set(int value);

	void increment();

	void decrement();

	default NbtInt write() {
		return NbtInt.of(this.get());
	}

	default void read(NbtInt nbt) {
		this.set(nbt.intValue());
	}

	default byte nbtType() {
		return NbtElement.INT_TYPE;
	}
}
