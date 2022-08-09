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

package org.quiltmc.qsl.recipe.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.recipe.api.Recipes;
import org.quiltmc.qsl.recipe.api.brewing.AbstractBrewingRecipe;
import org.quiltmc.qsl.recipe.impl.RecipeImpl;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity implements SidedInventory {
	protected BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Shadow
	private static void craft(World world, BlockPos pos, DefaultedList<ItemStack> slots) {
		throw new IllegalArgumentException("Mixin injection failed.");
	}
	@Shadow
	int fuel;
	@Shadow
	int brewTime;
	@Unique
	private AbstractBrewingRecipe<?> quilt$recipe;

	@SuppressWarnings("ConstantConditions")
	@ModifyVariable(
			method = "tick",
			at = @At(
					value = "STORE",
					target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;canCraft(Lnet/minecraft/util/collection/DefaultedList;)Z"
			),
			ordinal = 0
	)
	private static boolean canCraftBrewingRecipe(boolean canCraft, World world, BlockPos pos, BlockState state, BrewingStandBlockEntity brewingStand) {
		var maybeRecipe = world.getRecipeManager().getFirstMatch(Recipes.BREWING, brewingStand, world);
		var recipeHolder = ((BrewingStandBlockEntityMixin) (Object) brewingStand);
		recipeHolder.quilt$recipe = maybeRecipe.orElse(recipeHolder.quilt$recipe);
		return maybeRecipe.isPresent() || canCraft;
	}

	@SuppressWarnings("ConstantConditions")
	@Redirect(method = "tick",
			  at = @At(
					   value = "INVOKE",
					   target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;craft(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V"
			  )
	)
	private static void craftBrewingRecipe(World world, BlockPos pos, DefaultedList<ItemStack> slots) {
		BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) world.getBlockEntity(pos);
		BrewingStandBlockEntityMixin recipeHolder = (BrewingStandBlockEntityMixin) (Object) brewingStand;
		if (recipeHolder.quilt$recipe.matches(brewingStand, world)) {
			recipeHolder.quilt$recipe.craft(brewingStand);

			ItemStack ingredient = slots.get(3);

			// TODO take into account recipe remainder api
			ingredient.decrement(1);
			if (ingredient.getItem().hasRecipeRemainder()) {
				ItemStack remainder = new ItemStack(ingredient.getItem().getRecipeRemainder());
				if (ingredient.isEmpty()) {
					ingredient = remainder;
				} else {
					ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainder);
				}
			}

			slots.set(3, ingredient);
			world.syncWorldEvent(1035, pos, 0);

		} else {
			craft(world, pos, slots);
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;fuel:I", opcode = Opcodes.PUTFIELD, ordinal = 1))
	private static void modifyFuelUse(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity brewingStand, CallbackInfo ci) {
		var recipeHolder = (BrewingStandBlockEntityMixin) (Object) brewingStand;
		if (recipeHolder.quilt$recipe.matches(brewingStand, world)) {
			recipeHolder.fuel -= recipeHolder.quilt$recipe.getFuelUse() - 1; // Minus 1 because the base method already subtracts one
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "tick",
			at = @At(value = "FIELD",
					 target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;brewTime:I",
					 opcode = Opcodes.PUTFIELD,
					 ordinal = 2,
					 shift = At.Shift.AFTER
			)
	)
	private static void modifyBrewTime(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity brewingStand, CallbackInfo ci) {
		var recipeHolder = (BrewingStandBlockEntityMixin) (Object) brewingStand;
		if (recipeHolder.quilt$recipe.matches(brewingStand, world)) {
			// A brew time of 1 would be the equivalent of a 0 tick recipe
			recipeHolder.brewTime = Math.max(recipeHolder.quilt$recipe.getBrewTime(), 1);
		}
	}

	@Inject(method = "isValid", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
	private void isValidPotionItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(stack.isIn(RecipeImpl.VALID_INPUTS) && this.getStack(slot).isEmpty());
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void readBurnTimeAsInt(NbtCompound nbt, CallbackInfo info) {
		this.brewTime = nbt.getInt("BrewTime");
	}

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void writeBurnTimeAsInt(NbtCompound nbt, CallbackInfo info) {
		nbt.putInt("BrewTime", this.brewTime);
	}
}
