package org.quiltmc.qsl.recipe.api.data;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;

public final class IngredientData {
    public static IngredientData of(ImmutableList<Item> items) {
        return new IngredientData(Either.left(items));
    }

    public static IngredientData of(TagKey<Item> tag) {
        return new IngredientData(Either.right(tag));
    }

    private final Either<ImmutableList<Item>, TagKey<Item>> delegate;

    private IngredientData(Either<ImmutableList<Item>, TagKey<Item>> delegate) {
        this.delegate = delegate;
    }

    public DataResult<Ingredient> createIngredient(HolderLookup.Provider registries) {
        return this.delegate.map(
            items -> DataResult.success(Ingredient.ofItems(items.toArray(Item[]::new))),
            tag -> registries.getLookupOrThrow(RegistryKeys.ITEM).getTag(tag)
                .map(Ingredient::ofItems)
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "missing tag ingredient: " + tag.id()))
        );
    }
}
