package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.Lazy;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.NbtComponent;
import org.quiltmc.qsl.component.api.component.SyncedComponent;
import org.quiltmc.qsl.component.api.component.TickingComponent;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.sync.SyncChannel;

import java.util.function.Supplier;

public class SingleComponentContainer<C extends Component> implements ComponentContainer {
	private final ComponentType<C> type;
	private final Maybe<SyncChannel<?>> syncChannel;
	private final boolean ticking;
	private Lazy<? extends C> entry;
	private boolean shouldSync = false;

	protected SingleComponentContainer(ComponentType<C> type, boolean ticking, @Nullable SyncChannel<?> syncChannel) {
		this.type = type;
		this.ticking = ticking;
		this.syncChannel = Maybe.wrap(syncChannel);
	}

	public static <C extends Component> ComponentContainer.Factory<SingleComponentContainer<C>> createFactory(ComponentEntry<C> entry) {
		return (provider, ignored, saveOperation, ticking, syncChannel) -> {
			ComponentType<C> type = entry.type();
			var container = new SingleComponentContainer<>(type, ticking, syncChannel);
			container.setEntry(() -> entry.apply(saveOperation, () -> container.shouldSync = true));

			return container;
		};
	}

	@Override
	public Maybe<Component> expose(ComponentType<?> type) {
		return type == this.type ? Maybe.just(this.entry.get()) : Maybe.nothing();
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		this.entry.ifFilled(c -> {
			if (c instanceof NbtComponent<?> nbtComponent) {
				NbtComponent.writeTo(providerRootNbt, nbtComponent, this.type.id());
			}
		});
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		String idString = this.type.id().toString();
		if (providerRootNbt.getKeys().contains(idString)) {
			this.expose(this.type)
				.map(it -> ((NbtComponent<?>) it))
				.ifJust(nbtComponent -> {
					NbtComponent.readFrom(nbtComponent, this.type.id(), providerRootNbt);

					if (this.syncChannel.isJust()) {
						((SyncedComponent) nbtComponent).sync();
					}
				});
		}
	}

	@Override
	public void tick(ComponentProvider provider) {
		if (this.ticking) {
			this.expose(this.type)
				.map(it -> ((TickingComponent) it))
				.ifJust(tickingComponent -> tickingComponent.tick(provider));
		}
	}

	@Override
	public void sync(ComponentProvider provider) {
		if (this.shouldSync) {
			this.syncChannel.ifJust(channel -> channel.send(provider, buf -> {
				buf.writeInt(1); // size will always be one in this case.
				ComponentType.NETWORK_CODEC.encode(buf, this.type); // append the type rawId
				((SyncedComponent) this.entry.get()).writeToBuf(buf); // append component data
			}));
		}
	}

	private void setEntry(Supplier<? extends C> supplier) {
		this.entry = Lazy.of(supplier);
	}
}
