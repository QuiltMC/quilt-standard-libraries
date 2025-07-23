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

import static org.quiltmc.qsl.registry.attachment.test.AttachmentTestUtil.registerItem;
import static org.quiltmc.qsl.registry.attachment.test.AttachmentTestUtil.registerItemWithExtension;

import java.util.Iterator;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

public class DispatchedAttachmentTest implements ModInitializer, ResourceLoaderEvents.EndDataPackReload {
	public static final Logger LOGGER = LogUtils.getLogger();

	private static final RegistryEntryAttachment<Item, FuncValue> MODULAR_FUNCTION = RegistryEntryAttachment
			.dispatchedBuilder(
				Registries.ITEM, Identifier.of("quilt", "modular_function"),
				FuncValue.class, FuncValue.CODECS::get, FuncValue.PACKET_CODECS::get
			)
			.build();

	private static void registryModularFunctionItem(String path) {
		registerItem(path, ModularFunctionItem::new);
	}

	private static void registryModularFunctionItemWithExtension(String path, FuncValue value) {
		registerItemWithExtension(
				path, ModularFunctionItem::new,
				MODULAR_FUNCTION, value
		);
	}

	public static final class ModularFunctionItem extends Item {
		public ModularFunctionItem(Settings settings) {
			super(settings);
		}

		@Override
		public ActionResult use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient()) {
				ServerPlayerEntity player = (ServerPlayerEntity) user;
				MODULAR_FUNCTION.get(this).ifPresentOrElse(funcValue -> funcValue.invoke(player),
						() -> player.sendMessage(Text.literal("No function assigned!")
								.formatted(Formatting.RED), true));
			}

			return ActionResult.PASS;
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		// Has a built-in value of one type.
		registryModularFunctionItemWithExtension("modular_item_1", new SendMessageFuncValue("Built-in value!"));

		// Has a built-in value of one type, overridden via datapack by a value with another type.
		registryModularFunctionItemWithExtension("modular_item_2", new SendMessageFuncValue("Built-in value!"));

		// Set via datapack.
		registryModularFunctionItem("modular_item_3");

		// Has no value at all.
		registryModularFunctionItem("modular_item_4");

		// Has a value a provided by a tag.
		registryModularFunctionItem("modular_item_5");

		// Has a value a provided by a tag via datapack.
		registryModularFunctionItem("modular_item_6");

		MODULAR_FUNCTION.put(
				TagKey.of(RegistryKeys.ITEM, AttachmentTestUtil.createId("modular_tag_1")),
				new SendMessageFuncValue("Built-in value via tag!")
		);
	}

	@Override
	public void onEndDataPackReload(Context context) {
		if (context.error().isPresent()) {
			return;
		}

		LOGGER.info(" === DATA PACK RELOADED! === ");

		Iterator<RegistryEntryAttachment.TagEntry<Item, FuncValue>> tagItr = MODULAR_FUNCTION.tagEntryIterator();
		while (tagItr.hasNext()) {
			RegistryEntryAttachment.TagEntry<Item, FuncValue> entry = tagItr.next();
			LOGGER.info("Tag #{} is set to {}", entry.tag().id(), entry.value());
		}

		Iterator<RegistryEntryAttachment.Entry<Item, FuncValue>> itemItr = MODULAR_FUNCTION.entryIterator();
		while (itemItr.hasNext()) {
			RegistryEntryAttachment.Entry<Item, FuncValue> entry = itemItr.next();
			LOGGER.info("Entry {} is set to {}", Registries.ITEM.getId(entry.entry()), entry.value());
		}
	}
}
