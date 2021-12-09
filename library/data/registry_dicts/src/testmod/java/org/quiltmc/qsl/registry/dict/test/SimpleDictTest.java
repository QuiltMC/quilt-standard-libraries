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

package org.quiltmc.qsl.registry.dict.test;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import org.quiltmc.qsl.registry.dict.api.RegistryDict;
import org.quiltmc.qsl.registry.dict.api.RegistryExtensions;

public class SimpleDictTest implements ModInitializer {
	public static final RegistryDict<Item, Integer> TEST_DICT =
			RegistryDict.intBuilder(Registry.ITEM,
					new Identifier("quilt", "test_dict")).build();
	public static final RegistryDict<Item, Float> TEST_DICT_2 =
			RegistryDict.floatBuilder(Registry.ITEM,
					new Identifier("quilt", "test_dict_2")).build();

	public static final MyItem MY_ITEM = RegistryExtensions.registerWithDictValues(Registry.ITEM,
			new Identifier("quilt", "simple_dict_test_item"),
			new MyItem(new Item.Settings()),
			setter -> setter
					.put(TEST_DICT_2, 2.0f));

	@Override
	public void onInitialize() {
	}

	public static final class MyItem extends Item {
		public MyItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient) {
				int one = TEST_DICT.getValue(this)
						.orElseThrow(() -> new RuntimeException(TEST_DICT + " not set via datapack!"));
				float two = TEST_DICT_2.getValue(this)
						.orElseThrow(() -> new RuntimeException(TEST_DICT_2 + " not set via built-in!"));
				user.sendMessage(Text.of("Test1 = " + one + ", Test2 = " + two), true);
			}
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
	}
}
