package org.quiltmc.qsl.component.api.components;

import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.Component;

public interface NbtComponent<T extends NbtElement> extends Component {

	T write();

	void read(T nbt);

	byte nbtType();

	@SuppressWarnings("unchecked") // Suppressing because we know those values are hardcoded either way.
	static void forward(NbtComponent<?> nbtComponent, Identifier id, NbtCompound nbt) {
		switch (nbtComponent.nbtType()){
			case NbtElement.BYTE_TYPE -> {
				NbtByte nbtTarget = (NbtByte) nbt.get(id.toString());
				((NbtComponent<NbtByte>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.SHORT_TYPE -> {
				NbtShort nbtTarget = (NbtShort) nbt.get(id.toString());
				((NbtComponent<NbtShort>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.INT_TYPE -> {
				NbtInt nbtTarget = (NbtInt) nbt.get(id.toString());
				((NbtComponent<NbtInt>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.LONG_TYPE -> {
				NbtLong nbtTarget = (NbtLong) nbt.get(id.toString());
				((NbtComponent<NbtLong>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.FLOAT_TYPE -> {
				NbtFloat nbtTarget = (NbtFloat) nbt.get(id.toString());
				((NbtComponent<NbtFloat>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.DOUBLE_TYPE -> {
				NbtDouble nbtTarget = (NbtDouble) nbt.get(id.toString());
				((NbtComponent<NbtDouble>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.BYTE_ARRAY_TYPE -> {
				NbtByteArray nbtTarget = (NbtByteArray) nbt.get(id.toString());
				((NbtComponent<NbtByteArray>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.STRING_TYPE -> {
				NbtString nbtTarget = (NbtString) nbt.get(id.toString());
				((NbtComponent<NbtString>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.LIST_TYPE -> {
				NbtList nbtTarget = (NbtList) nbt.get(id.toString());
				((NbtComponent<NbtList>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.COMPOUND_TYPE -> {
				NbtCompound nbtTarget = (NbtCompound) nbt.get(id.toString());
				((NbtComponent<NbtCompound>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.INT_ARRAY_TYPE -> {
				NbtIntArray nbtTarget = (NbtIntArray) nbt.get(id.toString());
				((NbtComponent<NbtIntArray>) nbtComponent).read(nbtTarget);
			}
			case NbtElement.LONG_ARRAY_TYPE -> {
				NbtLongArray nbtTarget = (NbtLongArray) nbt.get(id.toString());
				((NbtComponent<NbtLongArray>) nbtComponent).read(nbtTarget);
			}
			default -> // TODO: Handle non-vanilla nbt types.
					throw new RuntimeException("The nbt data type with id %d is not handled at the moment!".formatted(nbtComponent.nbtType()));
		}
	}

}
