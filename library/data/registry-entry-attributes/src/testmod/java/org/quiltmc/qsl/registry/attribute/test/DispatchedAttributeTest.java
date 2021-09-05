/*
 * Copyright 2021 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.registry.attribute.test;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.ModInitializer;

import org.quiltmc.qsl.registry.attribute.api.DispatchedType;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryExtensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import java.util.Map;

public class DispatchedAttributeTest implements ModInitializer {
	public static abstract class FuncValue implements DispatchedType {
		// in a real-world application, you'd probably use a Registry for this
		public static final Map<Identifier, Codec<? extends FuncValue>> CODECS = Util.make(() ->
				ImmutableMap.<Identifier, Codec<? extends FuncValue>>builder()
						.put(SendMessageFuncValue.TYPE, SendMessageFuncValue.CODEC)
						.put(GiveStackFuncValue.TYPE, GiveStackFuncValue.CODEC)
						.build());

		protected final Identifier type;

		protected FuncValue(Identifier type) {
			this.type = type;
		}

		@Override
		public final Identifier getType() {
			return type;
		}

		public abstract void invoke(ServerPlayerEntity player);
	}

	public static final class SendMessageFuncValue extends FuncValue {
		public static final Identifier TYPE = new Identifier("quilt", "send_message");
		public static final Codec<SendMessageFuncValue> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(Codec.STRING.fieldOf("message").forGetter(sm -> sm.message))
						.apply(instance, SendMessageFuncValue::new));

		private final String message;

		public SendMessageFuncValue(String message) {
			super(TYPE);
			this.message = message;
		}

		@Override
		public void invoke(ServerPlayerEntity player) {
			player.sendMessage(Text.of("Quilt says: '" + message + "'"), false);
		}
	}

	public static final class GiveStackFuncValue extends FuncValue {
		public static final Identifier TYPE = new Identifier("quilt", "give_stack");
		public static final Codec<GiveStackFuncValue> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(ItemStack.CODEC.fieldOf("stack").forGetter(gs -> gs.stack))
						.apply(instance, GiveStackFuncValue::new));

		private final ItemStack stack;

		public GiveStackFuncValue(ItemStack stack) {
			super(TYPE);
			this.stack = stack;
		}

		@Override
		public void invoke(ServerPlayerEntity player) {
			player.getInventory().offerOrDrop(stack.copy());
		}
	}

	public static final RegistryEntryAttribute<Item, FuncValue> MODULAR_FUNCTION =
			RegistryEntryAttribute.<Item, FuncValue>dispatchedBuilder(Registry.ITEM, new Identifier("quilt", "modular_function"),
					FuncValue.CODECS::get).build();

	public static final class ModularFunctionItem extends Item {
		public ModularFunctionItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient()) {
				ServerPlayerEntity player = (ServerPlayerEntity) user;
				MODULAR_FUNCTION.getValue(this).ifPresentOrElse(funcValue -> funcValue.invoke(player),
						() -> player.sendMessage(new LiteralText("No function assigned!")
								.styled(style -> style.withColor(Formatting.RED)), true));
			}
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
	}

	// built-in of one type
	public static final ModularFunctionItem ITEM_1 = RegistryExtensions.registerWithAttributes(Registry.ITEM,
			new Identifier("quilt", "modular_item_1"), new ModularFunctionItem(new Item.Settings()),
			builder -> builder.put(MODULAR_FUNCTION, new SendMessageFuncValue("Built-in value!")));
	// built-in of one type, overriden by data-driven of another type
	public static final ModularFunctionItem ITEM_2 = RegistryExtensions.registerWithAttributes(Registry.ITEM,
			new Identifier("quilt", "modular_item_2"), new ModularFunctionItem(new Item.Settings()),
			builder -> builder.put(MODULAR_FUNCTION, new SendMessageFuncValue("Built-in value!")));
	// data-driven of some type
	public static final ModularFunctionItem ITEM_3 = Registry.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_3"), new ModularFunctionItem(new Item.Settings()));
	// no value at all
	public static final ModularFunctionItem ITEM_4 = Registry.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_4"), new ModularFunctionItem(new Item.Settings()));

	@Override
	public void onInitialize() { }
}
