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
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;

public class SimpleAttachmentTest implements ModInitializer {
	public static final RegistryEntryAttachment<Item, Integer> TEST_ATTACHMENT =
			RegistryEntryAttachment.intBuilder(Registry.ITEM,
					new Identifier("quilt", "test_attachment")).build();
	public static final RegistryEntryAttachment<Item, Float> TEST_ATTACHMENT_2 =
			RegistryEntryAttachment.floatBuilder(Registry.ITEM,
					new Identifier("quilt", "test_attachment_2")).build();

	public static final MyItem MY_ITEM = RegistryExtensions.register(Registry.ITEM,
			new Identifier("quilt", "simple_attachment_test_item"),
			new MyItem(new Item.Settings()),
			TEST_ATTACHMENT_2, 2.0f);

	@Override
	public void onInitialize(ModContainer mod) {}

	public static final class MyItem extends Item {
		public MyItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient) {
				int one = TEST_ATTACHMENT.get(this)
						.orElseThrow(() -> new RuntimeException(TEST_ATTACHMENT + " not set via datapack!"));
				float two = TEST_ATTACHMENT_2.get(this)
						.orElseThrow(() -> new RuntimeException(TEST_ATTACHMENT_2 + " not set via built-in!"));
				user.sendMessage(Text.of("Test1 = " + one + ", Test2 = " + two), true);
			}
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
	}
}
