package org.quiltmc.qsl.inworldrecipes.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents an in-world recipe.
 */
public final class InWorldRecipe {
	private final @NotNull Block targetBlock;
	private final @NotNull Predicate<ItemUsageContext> predicate;
	private final @NotNull Consumer<ItemUsageContext> action;

	/**
	 * Creates an in-world recipe.
	 * @param targetBlock target block
	 * @param predicate recipe predicate
	 * @param action action the recipe executes when completed
	 */
	public InWorldRecipe(@NotNull Block targetBlock,
						 @NotNull Predicate<ItemUsageContext> predicate,
						 @NotNull Consumer<ItemUsageContext> action) {
		this.targetBlock = targetBlock;
		this.predicate = predicate;
		this.action = action;
	}

	/**
	 * {@return the target block of this recipe}
	 */
	public @NotNull Block targetBlock() {
		return targetBlock;
	}

	/**
	 * {@return the predicate of this recipe}
	 */
	public @NotNull Predicate<ItemUsageContext> predicate() {
		return predicate;
	}

	/**
	 * {@return the action of this recipe}
	 */
	public @NotNull Consumer<ItemUsageContext> action() {
		return action;
	}
}
