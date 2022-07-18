package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;

// TODO: Make this work!!!
public class CompositeComponentContainer implements ComponentContainer {
	private final ComponentContainer main;
	private final ComponentContainer fallback;

	public CompositeComponentContainer(ComponentContainer main, ComponentContainer fallback) {
		this.main = main;
		this.fallback = fallback;
	}

	@Override
	public Maybe<Component> expose(ComponentType<?> type) {
		return this.main.expose(type).or(() -> this.fallback.expose(type));
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		this.main.writeNbt(providerRootNbt);
		this.fallback.writeNbt(providerRootNbt);
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		this.main.readNbt(providerRootNbt);
		this.fallback.readNbt(providerRootNbt);
	}

	@Override
	public void tick(ComponentProvider provider) {
		this.main.tick(provider);
		this.fallback.tick(provider);
	}

	@Override
	public void sync(ComponentProvider provider) {
		this.main.sync(provider);
		this.fallback.sync(provider);
	}
}
