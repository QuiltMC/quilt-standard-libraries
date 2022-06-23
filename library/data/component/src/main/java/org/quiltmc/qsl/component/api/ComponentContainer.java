package org.quiltmc.qsl.component.api;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public interface ComponentContainer {
	Optional<Component> expose(Identifier id);

	Map<Identifier, Component> exposeAll();

	void moveComponents(ComponentContainer other);

	void writeNbt(NbtCompound providerRootNbt);

	void readNbt(NbtCompound providerRootNbt);

	void setSaveOperation(@NotNull Runnable runnable);
}
