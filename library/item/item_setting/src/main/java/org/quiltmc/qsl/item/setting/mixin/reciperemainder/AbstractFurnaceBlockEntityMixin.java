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

package org.quiltmc.qsl.item.setting.mixin.reciperemainder;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.checkerframework.common.aliasing.qual.Unique;
import org.quiltmc.qsl.item.setting.impl.RecipeRemainderLogicHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BlockEntity implements SidedInventory {
	@Shadow
	protected DefaultedList<ItemStack> inventory;

	@Unique
	private static final ThreadLocal<AbstractFurnaceBlockEntity> quilt$threadLocalBlockEntity = new ThreadLocal<>();

	public AbstractFurnaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// Needed some place to store the furnace entity before any remainders are checked
	@Inject(method = "isBurning", at = @At("HEAD"))
	private void setThreadLocalBlockEntity(CallbackInfoReturnable<Boolean> cir) {
		quilt$threadLocalBlockEntity.set((AbstractFurnaceBlockEntity) (BlockEntity) this);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void setFuelRemainder(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci, boolean burning, boolean dirty, ItemStack fuelStack, boolean hasInput, boolean hasFuel, Recipe<?> recipe, int maxCount, Item fuel) {
		// TODO: test
		RecipeRemainderLogicHandler.handleRemainderForNonPlayerCraft(
				fuelStack,
				recipe,
				((AbstractFurnaceBlockEntityMixin) (BlockEntity) blockEntity).inventory,
				1,
				blockEntity.getWorld(),
				blockEntity.getPos()
		);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
	private static <E> E setRemainder(DefaultedList<E> defaultedList, int index, E element) {
		return element;
	}

	@Redirect(method = "craftRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private static void setInputRemainder(ItemStack inputStack, int amount, Recipe<?> recipe, DefaultedList<ItemStack> slots, int count) {
		RecipeRemainderLogicHandler.handleRemainderForNonPlayerCraft(
				inputStack,
				recipe,
				slots,
				0,
				quilt$threadLocalBlockEntity.get().getWorld(),
				quilt$threadLocalBlockEntity.get().getPos()
		);
		inputStack.decrement(amount);
	}
}
