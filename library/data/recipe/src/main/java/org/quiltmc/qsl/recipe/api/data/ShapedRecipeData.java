package org.quiltmc.qsl.recipe.api.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.CraftingRecipeInput;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapedRecipePattern;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;

import static java.util.Objects.requireNonNull;

public final class ShapedRecipeData implements RecipeData<CraftingRecipeInput, ShapedRecipe> {
    private static final int MIN_WIDTH = 1;
    private static final int MAX_WIDTH = 3;
    private static final int MIN_HEIGHT = 1;
    private static final int MAX_HEIGHT = 3;

    private final String group;
    private final CraftingCategory category;
    private final ImmutableList<String> pattern;
    private final ImmutableMap<Character, Either<Item, TagKey<Item>>> key;
    private final ItemStack result;
    private final boolean showNotification;

    private final int width;
    private final int height;

    public ShapedRecipeData(
        String group,
        CraftingCategory category,
        ImmutableList<String> pattern,
        ImmutableMap<Character, Either<Item, TagKey<Item>>> key,
        ItemStack result,
        boolean showNotification
    ) {
        requireNonNull(pattern, "pattern must not be null");
        requireNonNull(key, "key must not be null");

        this.height = pattern.size();
        if (this.height < MIN_HEIGHT || this.height > MAX_HEIGHT) {
            throw new IllegalArgumentException(
                "pattern height must be between %s and %s; was %s"
                    .formatted(MIN_HEIGHT, MAX_HEIGHT, this.height)
            );
        }

        final Iterator<String> rowItr = pattern.iterator();
        this.width = rowItr.next().length();
        if (this.width < MIN_WIDTH || this.width > MAX_WIDTH) {
            throw new IllegalArgumentException(
                "pattern width must be between %s and %s; was %s"
                    .formatted(MIN_WIDTH, MAX_WIDTH, this.width)
            );
        }

        while (rowItr.hasNext()) {
            final int currentWidth = rowItr.next().length();
            if (currentWidth != this.width) {
                throw new IllegalArgumentException(
                    "all pattern rows must have the same width; found both %s and %s"
                        .formatted(this.width, currentWidth)
                );
            }
        }

        final Set<Character> patternSymbols = new HashSet<>();
        for (int col = 0; col < this.height; col++) {
            for (int row = 0; row < this.width; row++) {
                final char symbol = pattern.get(col).charAt(row);
                if (symbol != ShapedRecipePattern.EMPTY_SLOT && !key.containsKey(symbol)) {
                    throw new IllegalArgumentException("key has no mapping for " + symbol);
                } else {
                    patternSymbols.add(symbol);
                }
            }
        }

        for (final Character keySymbol : key.keySet()) {
            if (keySymbol == ShapedRecipePattern.EMPTY_SLOT) {
                throw new IllegalArgumentException(
                    "key must not map '%1$s'; '%1$s' is reserved for empty slots"
                        .formatted(ShapedRecipePattern.EMPTY_SLOT)
                );
            } else if (!patternSymbols.contains(keySymbol)) {
                throw new IllegalArgumentException(
                    "key contains extra mapping that doesn't appear in pattern: " + keySymbol
                );
            }
        }

        this.group = requireNonNull(group, "group must not be null");
        this.category = requireNonNull(category, "category must not be null");
        this.pattern = pattern;
        this.key = key;
        this.result = requireNonNull(result, "result must not be null");
        this.showNotification = showNotification;
    }

    @Override
    public DataResult<ShapedRecipe> createRecipe(HolderLookup.Provider registries) {
        final HolderLookup.RegistryLookup<Item> items = registries.getLookup(RegistryKeys.ITEM).orElseThrow();

        final ImmutableList<DataResult<Optional<Ingredient>>> ingredientResults = this.pattern.stream()
            .flatMap(row -> row.chars().mapToObj(c -> (char)c))
            .<DataResult<Optional<Ingredient>>>map(symbol -> {
                if (symbol == ShapedRecipePattern.EMPTY_SLOT) {
                    return DataResult.success(Optional.empty());
                } else {
                    //noinspection DataFlowIssue; the contructor verifies this is safe
                    return this.key.get(symbol).map(
                        item -> DataResult.success(Optional.of(Ingredient.ofItem(item))),
                        tag -> items.getTag(tag)
                            .map(Ingredient::ofItems)
                            .map(Optional::of)
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "missing tag ingredient: " + tag.id()))
                    );
                }
            })
            .collect(toImmutableList());

        final String errorMessage = ingredientResults.stream()
            .map(DataResult::error)
            .flatMap(Optional::stream)
            .map(DataResult.Error::message)
            .collect(Collectors.joining(", "));

        if (!errorMessage.isEmpty()) {
            return DataResult.error(() -> errorMessage);
        }

        final ImmutableList<Optional<Ingredient>> ingredients = ingredientResults.stream()
            .map(DataResult::getOrThrow)
            .collect(toImmutableList());

        return DataResult.success(new ShapedRecipe(
            this.group, this.category,
            new ShapedRecipePattern(
                this.width, this.height,
                ingredients,
                Optional.empty()
            ),
            this.result,
            this.showNotification
        ));
    }
}
