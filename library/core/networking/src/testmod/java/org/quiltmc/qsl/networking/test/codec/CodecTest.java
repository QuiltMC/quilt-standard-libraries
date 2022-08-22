package org.quiltmc.qsl.networking.test.codec;

import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.BOOLEAN;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.BYTE;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.NBT;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.constant;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.indexOf;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class CodecTest implements ModInitializer {
	public static final NetworkCodec<ItemStack> NON_EMPTY_ITEM_STACK = NetworkCodec.build(builder -> builder.create(
			indexOf(Registry.ITEM).fieldOf(ItemStack::getItem),
			BYTE.fieldOf(stack -> (byte) stack.getCount()),
			NBT.fieldOf(ItemStack::getNbt)
	).apply((item, count, nbt) -> {
		var stack = new ItemStack(item, count);

		if (nbt != null) {
			stack.setNbt(nbt);
		}

		return stack;
	}).named("NonEmptyItemStack"));

	public static final NetworkCodec<ItemStack> ITEM_STACK = BOOLEAN.dispatch(
			stack -> !stack.isEmpty(),
			hasItem -> hasItem ? NON_EMPTY_ITEM_STACK : constant(ItemStack.EMPTY)
	).named("ItemStack");

	@Override
	public void onInitialize(ModContainer mod) {
		System.out.println(ITEM_STACK);
		PacketByteBuf buf = ITEM_STACK.createBuffer(ItemStack.EMPTY);

		System.out.println(buf.readableBytes());

		if (!ITEM_STACK.decode(buf).isEmpty()) {
			throw new IllegalStateException("Decode failed");
		}

		ItemStack acaciaStack = new ItemStack(Items.ACACIA_FENCE, 12);
		NbtCompound sub = new NbtCompound();
		sub.putString("Hello", "World");
		sub.putInt("Int", 42);
		sub.putDouble("Float", 3.14159265d);
		acaciaStack.getOrCreateNbt().put("SubTag", sub);

		var buf2 = NetworkCodec.ITEM_STACK.createBuffer(acaciaStack);
		System.out.println(buf2.readableBytes());

		var res = ITEM_STACK.decode(buf2);
		System.out.println(res + res.getNbt().toString());
	}
}
