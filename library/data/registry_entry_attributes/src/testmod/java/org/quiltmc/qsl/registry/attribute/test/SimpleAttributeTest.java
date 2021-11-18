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

import net.fabricmc.api.ModInitializer;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryExtensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SimpleAttributeTest implements ModInitializer {
	public static final RegistryEntryAttribute<Item, Integer> TEST_ATTRIBUTE =
			RegistryEntryAttribute.intBuilder(Registry.ITEM,
					new Identifier("quilt", "test_attribute")).build();
	public static final RegistryEntryAttribute<Item, Float> TEST_ATTRIBUTE_2 =
			RegistryEntryAttribute.floatBuilder(Registry.ITEM,
					new Identifier("quilt", "test_attribute_2")).build();

	public static final MyItem MY_ITEM = RegistryExtensions.registerWithAttributes(Registry.ITEM,
			new Identifier("quilt", "simple_attribute_test_item"),
			new MyItem(new Item.Settings()),
			setter -> setter
					.put(TEST_ATTRIBUTE_2, 2.0f));

	@Override
	public void onInitialize() { }

	public static final class MyItem extends Item {
		public MyItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient) {
				int one = TEST_ATTRIBUTE.getValue(this)
						.orElseThrow(() -> new RuntimeException(TEST_ATTRIBUTE + " not set via datapack!"));
				float two = TEST_ATTRIBUTE_2.getValue(this)
						.orElseThrow(() -> new RuntimeException(TEST_ATTRIBUTE_2 + " not set via built-in!"));
				user.sendMessage(Text.of("Test1 = " + one + ", Test2 = " + two), true);
			}
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
	}
}
