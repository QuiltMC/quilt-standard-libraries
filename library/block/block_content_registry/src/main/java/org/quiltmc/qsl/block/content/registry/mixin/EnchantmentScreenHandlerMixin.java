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

package org.quiltmc.qsl.block.content.registry.mixin;

import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.block.Block;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {
	@Redirect(method = "m_mpsetdhw", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
	private Iterator<?> iterator(List<?> instance) {
		// Cancel old loop
		return ObjectIterators.emptyIterator();
	}

	@ModifyVariable(method = "m_mpsetdhw", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;setSeed(J)V"))
	private int quilt$m_mpsetdhw$ix(int old, ItemStack itemStack, World world, BlockPos pos) {
		// Round sum of powers to the nearest integer, x.5 is rounded down to x
		return -Math.round(-this.calculateBookshelfCount(world, pos));
	}

	@Unique
	private float calculateBookshelfCount(World world, BlockPos pos) {
		float count = 0;

		for (BlockPos offset : EnchantingTableBlock.POSSIBLE_BOOKSHELF_LOCATIONS) {
			if (world.isAir(pos.add(offset.getX() / 2, offset.getY(), offset.getZ() / 2))) {
				Block block = world.getBlockState(pos.add(offset)).getBlock();
				count += BlockContentRegistries.ENCHANTMENT_BOOSTER.get(block).orElse(0f);
			}
		}

		return count;
	}
}
