package org.quiltmc.qsl.component.api.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericComponent<T> implements NbtComponent<NbtCompound> {
	protected final Codec<T> codec;
	protected T value;
	@Nullable
	private Runnable saveOperation;

	protected GenericComponent(@NotNull Codec<T> codec) {
		this.codec = codec;
	}

	@NotNull
	public static <T> GenericComponent<T> create(@NotNull Codec<T> codec) {
		return new GenericComponent<>(codec);
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public byte nbtType() {
		return NbtElement.COMPOUND_TYPE;
	}

	@Override
	public void read(NbtCompound nbt) {
		this.codec.decode(NbtOps.INSTANCE, nbt.get("Value")).result()
				.map(Pair::getFirst)
				.ifPresent(this::setValue);
	}

	@Override
	public NbtCompound write() {
		var nbt = new NbtCompound();
		if (this.value != null) {
			this.codec.encodeStart(NbtOps.INSTANCE, this.value).result()
					.ifPresent(nbtElement -> {
						nbt.put("Value", nbtElement);
					});
		}
		return nbt;
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.saveOperation;
	}

	@Override
	public void setSaveOperation(@Nullable Runnable runnable) {
		this.saveOperation = runnable;
	}
}
