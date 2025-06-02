package org.quiltmc.qsl.recipe.api.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

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

    public static ShapedRecipeData of(
        @NotNull
        String group,
        @NotNull
        CraftingCategory category,
        @NotNull
        ImmutableList<String> pattern,
        @NotNull
        ImmutableMap<Character, Either<Item, TagKey<Item>>> key,
        @NotNull
        ItemStack result,
        boolean showNotification
    ) {
        requireSpecified(pattern, "pattern");
        requireSpecified(key, "key");

        final Dimensions patternDimensions = verifyPattern(pattern, key);

        return new ShapedRecipeData(
            requireSpecified(group, "group"),
            requireSpecified(category, "category"),
            pattern,
            patternDimensions.width, patternDimensions.height,
            key,
            requireSpecified(result, "result"),
            showNotification
        );
    }

    private static Dimensions verifyPattern(
        ImmutableList<String> pattern,
        ImmutableMap<Character, Either<Item, TagKey<Item>>> key
    ) {
        final int height = pattern.size();
        if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
            throw new IllegalArgumentException(
                "pattern height must be between %s and %s; was %s"
                    .formatted(MIN_HEIGHT, MAX_HEIGHT, height)
            );
        }

        final Iterator<String> rowItr = pattern.iterator();
        final int width = rowItr.next().length();
        if (width < MIN_WIDTH || width > MAX_WIDTH) {
            throw new IllegalArgumentException(
                "pattern width must be between %s and %s; was %s"
                    .formatted(MIN_WIDTH, MAX_WIDTH, width)
            );
        }

        while (rowItr.hasNext()) {
            final int currentWidth = rowItr.next().length();
            if (currentWidth != width) {
                throw new IllegalArgumentException(
                    "all pattern rows must have the same width; found both %s and %s"
                        .formatted(width, currentWidth)
                );
            }
        }

        final Set<Character> patternSymbols = new HashSet<>();
        for (int col = 0; col < height; col++) {
            for (int row = 0; row < width; row++) {
                final char symbol = pattern.get(col).charAt(row);
                if (symbol != ShapedRecipePattern.EMPTY_SLOT && !key.containsKey(symbol)) {
                    throw new IllegalArgumentException("key has no mapping for " + symbol);
                } else {
                    patternSymbols.add(symbol);
                }
            }
        }

        for (final Character keySymbol : key.keySet()) {
            if (!patternSymbols.contains(requireValidSymbol(keySymbol))) {
                throw new IllegalArgumentException(
                    "key contains extra mapping that doesn't appear in pattern: " + keySymbol
                );
            }
        }

        return new Dimensions(width, height);
    }

    private static char requireValidSymbol(char symbol) {
        if (symbol == ShapedRecipePattern.EMPTY_SLOT) {
            throw new IllegalArgumentException(
                "key must not map '%1$s'; '%1$s' is reserved for empty slots"
                    .formatted(ShapedRecipePattern.EMPTY_SLOT)
            );
        }

        return symbol;
    }

    private static <T> T requireSpecified(T value, String name) {
        return requireNonNull(value, name + " must be specified");
    }

    private ShapedRecipeData(
        @NotNull
        String group,
        @NotNull
        CraftingCategory category,
        @NotNull
        ImmutableList<String> pattern,
        int width, int height,
        @NotNull
        ImmutableMap<Character, Either<Item, TagKey<Item>>> key,
        @NotNull
        ItemStack result,
        boolean showNotification
    ) {
        this.group = group;
        this.category = category;
        this.pattern = pattern;
        this.width = width;
        this.height = height;
        this.key = key;
        this.result = result;
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
                    //noinspection DataFlowIssue; verifyPattern ensures this is safe
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

    public static final class Builder {
        private String group = "";
        private CraftingCategory category = CraftingCategory.MISC;
        private ImmutableList<String> pattern;
        private final ImmutableMap.Builder<Character, Either<Item, TagKey<Item>>> key = ImmutableMap.builder();
        private ItemStack result;
        private boolean showNotification = true;

        public Builder group(@NotNull String group) {
            this.group = requireNonNull(group);
            return this;
        }

        public Builder category(@NotNull CraftingCategory category) {
            this.category = requireNonNull(category);
            return this;
        }

        public Builder pattern(@NotNull Iterable<String> pattern) {
            this.pattern = ImmutableList.copyOf(requireNonNull(pattern));
            return this;
        }

        public Builder pattern(@NotNull String... pattern) {
            this.pattern = ImmutableList.copyOf(requireNonNull(pattern));
            return this;
        }

        public Builder ingredient(char symbol, @NotNull Item item) {
            this.key.put(requireValidSymbol(symbol), Either.left(requireNonNull(item)));
            return this;
        }

        public Builder ingredient(char symbol, @NotNull TagKey<Item> tag) {
            this.key.put(requireValidSymbol(symbol), Either.right(requireNonNull(tag)));
            return this;
        }

        public Builder result(@NotNull ItemStack result) {
            this.result = requireNonNull(result);
            return this;
        }

        public Builder showNotification(boolean show) {
            this.showNotification = show;
            return this;
        }

        public ShapedRecipeData build() {
            return ShapedRecipeData.of(
                this.group, this.category,
                this.pattern, this.key.build(),
                this.result,
                this.showNotification
            );
        }
    }

    private record Dimensions(int width, int height) { }
}
