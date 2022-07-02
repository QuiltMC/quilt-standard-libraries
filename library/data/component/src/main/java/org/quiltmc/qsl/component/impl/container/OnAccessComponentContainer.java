package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

// Suggestion from Technici4n from fabric. May help improve performance and memory footprint once done.
public class OnAccessComponentContainer implements ComponentContainer { // TODO: Check how to make this work?!
	private final Map<ComponentType<?>, Component> components;

	private OnAccessComponentContainer() {
		this.components = new IdentityHashMap<>();
	}

	@Override
	public Optional<Component> expose(ComponentType<?> type) {
		if (this.components.containsKey(type)) {
			return Optional.ofNullable(this.components.get(type));
		}

		Component created = type.create();
		this.components.put(type, created);
		return Optional.of(created);
	}

	@Override
	public void writeNbt(@NotNull NbtCompound providerRootNbt) {

	}

	@Override
	public void readNbt(@NotNull NbtCompound providerRootNbt) {

	}

	@Override
	public void tick(@NotNull ComponentProvider provider) {

	}

	@Override
	public void receiveSyncPacket(@NotNull ComponentType<?> type, @NotNull PacketByteBuf buf) {

	}

	@Override
	public void sync(@NotNull ComponentProvider provider) {

	}
}
