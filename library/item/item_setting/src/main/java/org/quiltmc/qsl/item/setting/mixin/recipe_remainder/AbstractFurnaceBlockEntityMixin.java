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

import com.llamalad7.mixinextras.sugar.Local;
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

import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.SingleRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderLogicHandler;
import org.quiltmc.qsl.item.setting.impl.RecipeRemainderLogicHandlerImpl;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract class AbstractFurnaceBlockEntityMixin extends BlockEntity implements SidedInventory {
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
	private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> recipeCache;

	@SuppressWarnings("DataFlowIssue")
	private AbstractFurnaceBlockEntityMixin() {
		super(null, null, null);
		throw new AssertionError("dummy constructor called");
	}

	// Needed some place to store the furnace entity before any remainders are checked
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "isBurning", at = @At("HEAD"))
	private void setThreadLocalBlockEntity(CallbackInfoReturnable<Boolean> cir) {
		quilt$THREAD_LOCAL_BLOCK_ENTITY.set((AbstractFurnaceBlockEntity) (BlockEntity) this);
	}

	// prevent additional smelting if remainder item overflow would have no location to be dropped into the world
	@Inject(method = "canAcceptRecipeOutput", at = @At("RETURN"), cancellable = true)
	private static void checkMismatchedRemaindersCanDrop(
			DynamicRegistryManager registryManager, RecipeHolder<? extends AbstractCookingRecipe> recipe,
			SingleRecipeInput singleRecipeInput, DefaultedList<ItemStack> slots, int count,
			CallbackInfoReturnable<Boolean> cir
	) {
		if (cir.getReturnValue() && quilt$THREAD_LOCAL_BLOCK_ENTITY.get() == null) {
			final ItemStack original = slots.get(INPUT_SLOT).copy();

			if (!original.isEmpty()) {
				final ItemStack remainder = RecipeRemainderLogicHandler
						.getRemainder(original, recipe.value(), RecipeRemainderLocation.FURNACE_INGREDIENT).copy();
				original.decrement(1);

				if (!remainder.isEmpty() && ItemStack.itemsAndComponentsMatch(original, remainder)) {
					final int toTake = Math.min(original.getMaxCount() - original.getCount(), remainder.getCount());
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
	private static void setFuelRemainder(
			ItemStack instance, int amount,
			@Local(argsOnly = true) ServerWorld world,
			@Local(argsOnly = true) AbstractFurnaceBlockEntity blockEntity
	) {
		final AbstractFurnaceBlockEntityMixin mixin = ((AbstractFurnaceBlockEntityMixin) (BlockEntity) blockEntity);

		final Recipe<?> recipe = mixin.inventory.get(INPUT_SLOT).isEmpty()
				? null
				: mixin.recipeCache.getFirstMatch(new SingleRecipeInput(mixin.inventory.get(INPUT_SLOT)), world)
				.map(RecipeHolder::value).orElse(null);

		RecipeRemainderLogicHandler.handleRemainderForNonPlayerCraft(
				instance,
				amount,
				recipe,
				RecipeRemainderLocation.FURNACE_FUEL,
				mixin.inventory,
				FUEL_SLOT,
				blockEntity.getWorld(),
				blockEntity.getPos()
		);
	}

	@Redirect(
			method = "tick",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"
			)
	)
	private static <E> E cancelVanillaRemainder(DefaultedList<E> defaultedList, int index, E element) {
		return element;
	}

	@Redirect(
			method = "craftRecipe",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V")
	)
	private static void setInputRemainder(
			ItemStack inputStack, int amount,
			@Local(argsOnly = true) @Nullable RecipeHolder<?> recipeHolder,
			@Local(argsOnly = true) DefaultedList<ItemStack> inventory
	) {
		RecipeRemainderLogicHandler.handleRemainderForNonPlayerCraft(
				inputStack,
				amount,
				recipeHolder == null ? null : recipeHolder.value(),
				RecipeRemainderLocation.FURNACE_INGREDIENT,
				inventory,
				INPUT_SLOT,
				// consumer only called when there are excess remainder items that can be dropped into the world
				remainder -> {
					// block entity could be null if another mixin allows craftRecipe to be called elsewhere
					// normally it's set in checkMismatchedRemaindersCanDrop before vanilla's only craftRecipe call
					@Nullable
					final AbstractFurnaceBlockEntity blockEntity = quilt$THREAD_LOCAL_BLOCK_ENTITY.get();
					if (blockEntity == null) {
						RecipeRemainderLogicHandlerImpl.LOGGER
							.warn("Unable to scatter excess remainder because block entity is null");
					} else {
						final BlockPos location = blockEntity.getPos();
						final World world = blockEntity.getWorld();
						if (world == null) {
							RecipeRemainderLogicHandlerImpl.LOGGER
								.warn("Unable to scatter excess remainder because block entity world is null");
						} else {
							ItemScatterer.spawn(
									world,
									location.getX(), location.getY(), location.getZ(),
									remainder
							);
						}
					}
				}
		);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private static void resetThreadLocalBlockEntity(
			ServerWorld world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci
	) {
		quilt$THREAD_LOCAL_BLOCK_ENTITY.remove();
	}
}
