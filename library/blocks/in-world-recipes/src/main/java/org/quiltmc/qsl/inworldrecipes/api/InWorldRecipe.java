package org.quiltmc.qsl.inworldrecipes.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents an in-world recipe.
 */
public final record InWorldRecipe(@NotNull Block targetBlock,
								  @NotNull Predicate<ItemUsageContext> predicate,
								  @NotNull Consumer<ItemUsageContext> action) { }
