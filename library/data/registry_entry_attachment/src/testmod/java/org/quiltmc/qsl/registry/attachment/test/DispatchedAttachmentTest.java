/*
 * Copyright 2021 The Quilt Project
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

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

public class DispatchedAttachmentTest implements ModInitializer,
		ResourceLoaderEvents.EndDataPackReload {
	public static final RegistryEntryAttachment<Item, FuncValue> MODULAR_FUNCTION =
			RegistryEntryAttachment.dispatchedBuilder(Registries.ITEM, new Identifier("quilt", "modular_function"),
					FuncValue.class, FuncValue.CODECS::get).build();

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final class ModularFunctionItem extends Item {
		public ModularFunctionItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient()) {
				ServerPlayerEntity player = (ServerPlayerEntity) user;
				MODULAR_FUNCTION.get(this).ifPresentOrElse(funcValue -> funcValue.invoke(player),
						() -> player.sendMessage(Text.literal("No function assigned!")
								.formatted(Formatting.RED), true));
			}

			return TypedActionResult.pass(user.getStackInHand(hand));
		}
	}

	/**
	 * Has a built-in value of one type.
	 */
	public static final ModularFunctionItem ITEM_1 = RegistryExtensions.register(Registries.ITEM,
			new Identifier("quilt", "modular_item_1"), new ModularFunctionItem(new Item.Settings()),
			MODULAR_FUNCTION, new SendMessageFuncValue("Built-in value!"));
	/**
	 * Has a built-in value of one type, overridden via datapack by a value with another type.
	 */
	public static final ModularFunctionItem ITEM_2 = RegistryExtensions.register(Registries.ITEM,
			new Identifier("quilt", "modular_item_2"), new ModularFunctionItem(new Item.Settings()),
			MODULAR_FUNCTION, new SendMessageFuncValue("Built-in value!"));
	/**
	 * Set via datapack.
	 */
	public static final ModularFunctionItem ITEM_3 = Registry.register(Registries.ITEM,
			new Identifier("quilt", "modular_item_3"), new ModularFunctionItem(new Item.Settings()));
	/**
	 * Has no value at all.
	 */
	public static final ModularFunctionItem ITEM_4 = Registry.register(Registries.ITEM,
			new Identifier("quilt", "modular_item_4"), new ModularFunctionItem(new Item.Settings()));
	/**
	 * Has a value a provided by a tag.
	 */
	public static final ModularFunctionItem ITEM_5 = Registry.register(Registries.ITEM,
			new Identifier("quilt", "modular_item_5"), new ModularFunctionItem(new Item.Settings()));
	/**
	 * Has a value a provided by a tag via datapack.
	 */
	public static final ModularFunctionItem ITEM_6 = Registry.register(Registries.ITEM,
			new Identifier("quilt", "modular_item_6"), new ModularFunctionItem(new Item.Settings()));

	@Override
	public void onInitialize(ModContainer mod) {
		MODULAR_FUNCTION.put(TagKey.of(RegistryKeys.ITEM, new Identifier("quilt", "modular_tag_1")),
				new SendMessageFuncValue("Built-in value via tag!"));
	}

	@Override
	public void onEndDataPackReload(Context context) {
		if (context.error().isPresent()) return;

		LOGGER.info(" === DATA PACK RELOADED! === ");

		var tagIt = MODULAR_FUNCTION.tagEntryIterator();
		while (tagIt.hasNext()) {
			var entry = tagIt.next();
			LOGGER.info("Tag #{} is set to {}", entry.tag().id(), entry.value());
		}

		var it = MODULAR_FUNCTION.entryIterator();
		while (it.hasNext()) {
			var entry = it.next();
			LOGGER.info("Entry {} is set to {}", Registries.ITEM.getId(entry.entry()), entry.value());
		}
	}
}
