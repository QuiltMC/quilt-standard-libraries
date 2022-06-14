package org.quiltmc.qsl.component.api.components;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.Component;

import java.util.Map;

public interface NbtComponent<T extends NbtElement> extends Component {

	T write();

	void read(T nbt);

	byte nbtType();

	@SuppressWarnings("unchecked") // Suppressing because we know those values are hardcoded either way.
	static void forward(NbtComponent<?> nbtComponent, Identifier id, NbtCompound nbt) {
		NbtElement nbtTarget = nbt.get(id.toString());
		switch (nbtComponent.nbtType()){
			case NbtElement.BYTE_TYPE -> ((NbtComponent<NbtByte>) nbtComponent).read((NbtByte) nbtTarget);
			case NbtElement.SHORT_TYPE -> ((NbtComponent<NbtShort>) nbtComponent).read((NbtShort) nbtTarget);
			case NbtElement.INT_TYPE -> ((NbtComponent<NbtInt>) nbtComponent).read((NbtInt) nbtTarget);
			case NbtElement.LONG_TYPE -> ((NbtComponent<NbtLong>) nbtComponent).read((NbtLong) nbtTarget);
			case NbtElement.FLOAT_TYPE -> ((NbtComponent<NbtFloat>) nbtComponent).read((NbtFloat) nbtTarget);
			case NbtElement.DOUBLE_TYPE -> ((NbtComponent<NbtDouble>) nbtComponent).read((NbtDouble) nbtTarget);
			case NbtElement.BYTE_ARRAY_TYPE -> ((NbtComponent<NbtByteArray>) nbtComponent).read((NbtByteArray) nbtTarget);
			case NbtElement.STRING_TYPE -> ((NbtComponent<NbtString>) nbtComponent).read((NbtString) nbtTarget);
			case NbtElement.LIST_TYPE -> ((NbtComponent<NbtList>) nbtComponent).read((NbtList) nbtTarget);
			case NbtElement.COMPOUND_TYPE -> ((NbtComponent<NbtCompound>) nbtComponent).read((NbtCompound) nbtTarget);
			case NbtElement.INT_ARRAY_TYPE -> ((NbtComponent<NbtIntArray>) nbtComponent).read((NbtIntArray) nbtTarget);
			case NbtElement.LONG_ARRAY_TYPE -> ((NbtComponent<NbtLongArray>) nbtComponent).read((NbtLongArray) nbtTarget);
			default -> // TODO: Handle non-vanilla nbt types.
					throw new RuntimeException("The nbt data type with id %d is not handled at the moment!".formatted(nbtComponent.nbtType()));
		}
	}

	static ImmutableMap<Identifier, NbtComponent<?>> getNbtSerializable(ImmutableMap<Identifier, Component> components) {
		var builder = ImmutableMap.<Identifier, NbtComponent<?>>builder();

		for (var entry : components.entrySet()) {
			if (entry.getValue() instanceof NbtComponent<?> nbtComponent) {
				builder.put(entry.getKey(), nbtComponent);
			}
		}

		return builder.build();
	}
}
