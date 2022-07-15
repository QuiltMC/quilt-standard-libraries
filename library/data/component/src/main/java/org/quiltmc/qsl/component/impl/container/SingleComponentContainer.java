package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
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
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;

import java.util.function.Supplier;

public class SingleComponentContainer<C extends Component> implements ComponentContainer {
	private final ComponentType<C> type;
	private final Maybe<SyncPacket.SyncContext> syncContext;
	private final boolean ticking;
	private Lazy<? extends C> entry;
	private boolean shouldSync = false;

	private SingleComponentContainer(ComponentType<C> type, boolean ticking, @Nullable SyncPacket.SyncContext syncContext) {
		this.type = type;
		this.ticking = ticking;
		this.syncContext = Maybe.wrap(syncContext);
	}

	private void setLazy(Supplier<? extends C> supplier) {
		this.entry = Lazy.of(supplier);
	}

	public static <C extends Component> ComponentContainer.Factory<SingleComponentContainer<C>> createFactory(ComponentEntry<C> entry) {
		return (provider, ignored, saveOperation, ticking, syncContext) -> {
			ComponentType<C> type = entry.type();
			var container = new SingleComponentContainer<>(type, ticking, syncContext);
			container.setLazy(() -> entry.apply(saveOperation, () -> container.shouldSync = true));

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
				.ifJust(nbtComponent -> NbtComponent.readFrom(nbtComponent, this.type.id(), providerRootNbt));
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
			this.syncContext.ifJust(ctx -> {
				PacketByteBuf buf = ctx.header().toBuffer(provider);
				buf.writeInt(1);
				ComponentType.NETWORK_CODEC.encode(buf, this.type);
				((SyncedComponent) this.entry.get()).writeToBuf(buf);
				SyncPacket.send(ctx, buf);
			});
		}
	}
}
