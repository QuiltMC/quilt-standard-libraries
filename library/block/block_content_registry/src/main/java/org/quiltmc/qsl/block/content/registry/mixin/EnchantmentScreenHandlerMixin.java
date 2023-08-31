/*
 * Copyright 2022 The Quilt Project
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

import java.util.Iterator;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectIterators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.block.content.registry.impl.BlockContentRegistriesImpl;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {
	// Lambda in onContentChanged at this.context.run((world,pos) ->
	@Redirect(
			method = "method_17411(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
			at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;")
	)
	private Iterator<?> iterator(List<?> instance) {
		// Cancel old loop
		return ObjectIterators.emptyIterator();
	}

	@ModifyVariable(
			method = "method_17411(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;setSeed(J)V")
	)
	private int quilt$method_17411$ix(int old, ItemStack itemStack, World world, BlockPos pos) {
		// Round sum of powers to the nearest integer, x.5 is rounded down to x
		return -Math.round(-BlockContentRegistriesImpl.calculateBookshelfCount(world, pos));
	}
}
