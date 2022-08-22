package org.quiltmc.qsl.networking.test.codec;

import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.BLOCK_POS;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.BOOLEAN;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.BYTE;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.NBT;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.constant;
import static org.quiltmc.qsl.networking.api.codec.NetworkCodec.indexOf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.channel.C2SNetworkChannel;
import org.quiltmc.qsl.networking.api.channel.NetworkChannel;
import org.quiltmc.qsl.networking.api.channel.S2CNetworkChannel;
import org.quiltmc.qsl.networking.api.channel.TwoWayNetworkChannel;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class CodecTest implements ModInitializer {
	public static final NetworkCodec<ItemStack> NON_EMPTY_ITEM_STACK = NetworkCodec.<ItemStack>build().create(
			indexOf(Registry.ITEM).fieldOf(ItemStack::getItem),
			BYTE.fieldOf(stack -> (byte) stack.getCount()),
			NBT.fieldOf(ItemStack::getNbt)
	).apply((item, count, nbt) -> {
		var stack = new ItemStack(item, count);

		if (nbt != null) {
			stack.setNbt(nbt);
		}

		return stack;
	}).named("NonEmptyItemStack");

	public static final NetworkCodec<ItemStack> ITEM_STACK = BOOLEAN.dispatch(
			stack -> !stack.isEmpty(),
			hasItem -> hasItem ? NON_EMPTY_ITEM_STACK : constant(ItemStack.EMPTY)
	).named("ItemStack");

	public static final C2SNetworkChannel<BlockPos> CHANNEL = NetworkChannel.createC2S(
			new Identifier("quiltmc", "test"),
			BLOCK_POS,
			() -> blockPos -> (server, sender, handler, responseSender) -> System.out.println("Hello channel world!")
	);

	public record Ctx(BlockPos pos, ItemStack stack) implements S2CNetworkChannel.Handler {
		public static final NetworkCodec<Ctx> CODEC = NetworkCodec.<Ctx>build().create(
				BLOCK_POS.fieldOf(Ctx::pos),
				ITEM_STACK.fieldOf(Ctx::stack)
		).apply(Ctx::new).named("Ctx");

		@Environment(EnvType.CLIENT)
		@Override
		public void clientHandle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketSender responseSender) {
			System.out.println("Hello client world!");
		}
	}
	public static final S2CNetworkChannel<Ctx> CHANNEL_2 =
			NetworkChannel.createS2C(new Identifier("quiltmc", "test"), Ctx.CODEC);

	public static final TwoWayNetworkChannel<ItemStack> STACK_CHANNEL = NetworkChannel.createTwoWay(
			new Identifier("quiltmc", "test"),
			ITEM_STACK,
			() -> stack -> (server, sender, handler, responseSender) -> System.out.println("Hello stack channel world!"),
			() -> stack -> (client, handler, responseSender) -> System.out.println("Hello stack channel world!")
	);

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
