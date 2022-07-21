package org.quiltmc.qsl.component.test.component;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.component.InventoryComponent;
import org.quiltmc.qsl.component.api.component.SyncedComponent;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;

import java.util.List;

public class ChunkInventoryComponent implements InventoryComponent, SyncedComponent {
	public static final NetworkCodec<List<ItemStack>> NETWORK_CODEC =
			NetworkCodec.list(NetworkCodec.ITEM_STACK, value -> DefaultedList.ofSize(value, ItemStack.EMPTY));

	private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(1, ItemStack.EMPTY);
	private final Operations ops;

	public ChunkInventoryComponent(Component.Operations ops) {
		this.ops = ops;
	}

	@Override
	public DefaultedList<ItemStack> getStacks() {
		return this.stacks;
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.ops.saveOperation();
	}

	@Override
	public void writeToBuf(PacketByteBuf buf) {
		NETWORK_CODEC.encode(buf, this.stacks);
	}

	@Override
	public void readFromBuf(PacketByteBuf buf) {
		List<ItemStack> receivedStacks = NETWORK_CODEC.decode(buf);
		for (int i = 0; i < receivedStacks.size(); i++) {
			this.stacks.set(i, receivedStacks.get(i));
		}
	}

	@Override
	public @Nullable Runnable getSyncOperation() {
		return this.ops.syncOperation();
	}
}
