package org.quiltmc.qsl.component.api.components;

import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;

public interface NbtComponent<T extends NbtElement> extends Component {

	static void readFrom(@NotNull NbtComponent<?> nbtComponent, @NotNull Identifier id, @NotNull NbtCompound nbt) {
		NbtElement nbtTarget = nbt.get(id.toString());
		read(nbtComponent, nbtTarget);
	}

	static void writeTo(@NotNull NbtCompound root, @NotNull NbtComponent<?> nbtComponent, @NotNull Identifier id) {
		root.put(id.toString(), nbtComponent.write());
	}

	@SuppressWarnings("unchecked")
	// Suppressing because we know those values are hardcoded either way.
	static void read(@NotNull NbtComponent<?> nbtComponent, NbtElement nbt) {
		switch (nbtComponent.nbtType()) {
			case NbtElement.BYTE_TYPE -> ((NbtComponent<NbtByte>) nbtComponent).read((NbtByte) nbt);
			case NbtElement.SHORT_TYPE -> ((NbtComponent<NbtShort>) nbtComponent).read((NbtShort) nbt);
			case NbtElement.INT_TYPE -> ((NbtComponent<NbtInt>) nbtComponent).read((NbtInt) nbt);
			case NbtElement.LONG_TYPE -> ((NbtComponent<NbtLong>) nbtComponent).read((NbtLong) nbt);
			case NbtElement.FLOAT_TYPE -> ((NbtComponent<NbtFloat>) nbtComponent).read((NbtFloat) nbt);
			case NbtElement.DOUBLE_TYPE -> ((NbtComponent<NbtDouble>) nbtComponent).read((NbtDouble) nbt);
			case NbtElement.BYTE_ARRAY_TYPE -> ((NbtComponent<NbtByteArray>) nbtComponent).read((NbtByteArray) nbt);
			case NbtElement.STRING_TYPE -> ((NbtComponent<NbtString>) nbtComponent).read((NbtString) nbt);
			case NbtElement.LIST_TYPE -> ((NbtComponent<NbtList>) nbtComponent).read((NbtList) nbt);
			case NbtElement.COMPOUND_TYPE -> ((NbtComponent<NbtCompound>) nbtComponent).read((NbtCompound) nbt);
			case NbtElement.INT_ARRAY_TYPE -> ((NbtComponent<NbtIntArray>) nbtComponent).read((NbtIntArray) nbt);
			case NbtElement.LONG_ARRAY_TYPE -> ((NbtComponent<NbtLongArray>) nbtComponent).read((NbtLongArray) nbt);
			default -> // TODO: Handle non-vanilla nbt types.
					throw new RuntimeException("The nbt data type with id %d is not handled at the moment!".formatted(nbtComponent.nbtType()));
		}
	}

	T write();

	void read(T nbt);

	byte nbtType();

	void saveNeeded();

	void setSaveOperation(@Nullable Runnable runnable);
}
