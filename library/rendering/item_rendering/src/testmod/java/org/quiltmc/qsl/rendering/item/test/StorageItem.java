/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.rendering.item.test;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class StorageItem extends Item {
	public static final int MAX = 1000;

	public StorageItem(Settings settings) {
		super(settings);
	}

	public int getCurrent(ItemStack stack) {
		var nbt = stack.getNbt();
		if (nbt == null) {
			return 0;
		}

		return nbt.getInt("current");
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack item = user.getStackInHand(hand).copy();

		var tag = item.getOrCreateNbt();
		int current = tag.getInt("current");

		if (current >= MAX) {
			current = 0; // cycle back to 0
		} else {
			current = Math.min(MAX, current + MAX / 5);
		}

		tag.putInt("current", current);

		return TypedActionResult.success(item);
	}
}
