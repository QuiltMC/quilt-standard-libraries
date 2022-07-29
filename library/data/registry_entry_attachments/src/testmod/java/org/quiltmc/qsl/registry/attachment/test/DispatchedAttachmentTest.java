/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.registry.attachment.test;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;

public class DispatchedAttachmentTest implements ModInitializer {
	public static final RegistryEntryAttachment<Item, FuncValue> MODULAR_FUNCTION =
			RegistryEntryAttachment.dispatchedBuilder(Registry.ITEM, new Identifier("quilt", "modular_function"),
					FuncValue.class, FuncValue.CODECS::get).build();

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

	/**
	 * Has a built-in value of one type.
	 */
	public static final ModularFunctionItem ITEM_1 = RegistryExtensions.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_1"), new ModularFunctionItem(new Item.Settings()),
			MODULAR_FUNCTION, new SendMessageFuncValue("Built-in value!"));
	/**
	 * Has a built-in value of one type, overridden via datapack by a value with another type.
	 */
	public static final ModularFunctionItem ITEM_2 = RegistryExtensions.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_2"), new ModularFunctionItem(new Item.Settings()),
			MODULAR_FUNCTION, new SendMessageFuncValue("Built-in value!"));
	/**
	 * Set via datapack.
	 */
	public static final ModularFunctionItem ITEM_3 = Registry.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_3"), new ModularFunctionItem(new Item.Settings()));
	/**
	 * Has no value at all.
	 */
	public static final ModularFunctionItem ITEM_4 = Registry.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_4"), new ModularFunctionItem(new Item.Settings()));
	/**
	 * Has a value a provided by a tag.
	 */
	public static final ModularFunctionItem ITEM_5 = Registry.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_5"), new ModularFunctionItem(new Item.Settings()));

	/**
	 * Has a value a provided by a tag via datapack.
	 */
	public static final ModularFunctionItem ITEM_6 = Registry.register(Registry.ITEM,
			new Identifier("quilt", "modular_item_6"), new ModularFunctionItem(new Item.Settings()));

	@Override
	public void onInitialize(ModContainer mod) {
		MODULAR_FUNCTION.put(TagKey.of(Registry.ITEM_KEY, new Identifier("quilt", "modular_tag_1")), new SendMessageFuncValue("Provided via tag"));
	}
}
