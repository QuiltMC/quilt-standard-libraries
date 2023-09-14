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

package org.quiltmc.qsl.item.setting.mixin.recipe_remainder;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLogicHandler;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BlockEntity implements SidedInventory {
	@Unique
	private static final ThreadLocal<AbstractFurnaceBlockEntity> quilt$THREAD_LOCAL_BLOCK_ENTITY = new ThreadLocal<>();
	@Shadow
	@Final
	protected static int FUEL_SLOT;
	@Shadow
	@Final
	protected static int INPUT_SLOT;
	@Shadow
	protected DefaultedList<ItemStack> inventory;
	@Shadow
	@Final
	private RecipeManager.CachedCheck<Inventory, ? extends AbstractCookingRecipe> recipeCache;

	public AbstractFurnaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// Needed some place to store the furnace entity before any remainders are checked
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "isBurning", at = @At("HEAD"))
	private void setThreadLocalBlockEntity(CallbackInfoReturnable<Boolean> cir) {
		quilt$THREAD_LOCAL_BLOCK_ENTITY.set((AbstractFurnaceBlockEntity) (BlockEntity) this);
	}

	// prevent additional smelting if remainder item overflow would have no location to be dropped into the world
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "canAcceptRecipeOutput", at = @At("RETURN"), cancellable = true)
	private static void checkMismatchedRemaindersCanDrop(DynamicRegistryManager registryManager, @Nullable Recipe<?> recipe, DefaultedList<ItemStack> inventory, int count, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && quilt$THREAD_LOCAL_BLOCK_ENTITY.get() == null) {
			ItemStack original = inventory.get(INPUT_SLOT).copy();

			if (!original.isEmpty()) {
				ItemStack remainder = RecipeRemainderLogicHandler.getRemainder(original, recipe).copy();
				original.decrement(1);

				if (!remainder.isEmpty() && ItemStack.canCombine(original, remainder)) {
					int toTake = Math.min(original.getMaxCount() - original.getCount(), remainder.getCount());
					remainder.decrement(toTake);

					if (!remainder.isEmpty()) {
						cir.setReturnValue(false);
					}
				}
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private static void setFuelRemainder(ItemStack fuelStack, int amount, World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
		AbstractFurnaceBlockEntityMixin cast = ((AbstractFurnaceBlockEntityMixin) (BlockEntity) blockEntity);

		Recipe<?> recipe;
		if (!cast.inventory.get(INPUT_SLOT).isEmpty()) {
			recipe = cast.recipeCache.getRecipeFor(blockEntity, world).orElse(null);
		} else {
			recipe = null;
		}

		RecipeRemainderLogicHandler.handleRemainderForNonPlayerCraft(
				fuelStack,
				amount,
				recipe,
				cast.inventory,
				FUEL_SLOT,
				blockEntity.getWorld(),
				blockEntity.getPos()
		);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
	private static <E> E cancelVanillaRemainder(DefaultedList<E> defaultedList, int index, E element) {
		return element;
	}

	@Redirect(method = "craftRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private static void setInputRemainder(ItemStack inputStack, int amount, DynamicRegistryManager registryManager, @Nullable Recipe<?> recipe, DefaultedList<ItemStack> inventory, int count) {
		RecipeRemainderLogicHandler.handleRemainderForNonPlayerCraft(
				inputStack,
				amount,
				recipe,
				inventory,
				INPUT_SLOT,
				remainder -> { // consumer only called when there are excess remainder items that can be dropped into the world
					// block entity guaranteed not to be null here thanks to checks in canAcceptRecipeOutput injection above
					AbstractFurnaceBlockEntity be = quilt$THREAD_LOCAL_BLOCK_ENTITY.get();
					BlockPos location = be.getPos();
					ItemScatterer.spawn(be.getWorld(), location.getX(), location.getY(), location.getZ(), remainder);
				}
		);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private static void resetThreadLocalBlockEntity(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
		quilt$THREAD_LOCAL_BLOCK_ENTITY.remove();
	}
}
