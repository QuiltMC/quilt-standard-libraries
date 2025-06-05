package org.quiltmc.qsl.recipe.api.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.CookingCategory;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SingleRecipeInput;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;

import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireResult;
import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireSpecified;

public final class VanillaRecipeData {
    private VanillaRecipeData() {
        throw new UnsupportedOperationException(
            VanillaRecipeData.class.getSimpleName() + "contains only static members"
        );
    }

    /**
     * Creates a {@link ShapedRecipeData.Builder} to aid in creating {@link ShapedRecipeData} instances.
     */
    public static ShapedRecipeData.Builder shapedBuilder() {
        return ShapedRecipeData.builder();
    }

    /**
     * Creates a {@link ShapelessRecipeData.Builder} to aid in creating {@link ShapelessRecipeData} instances
     */
    public static ShapelessRecipeData.Builder shapelessBuilder() {
        return ShapelessRecipeData.builder();
    }

    /**
     * Creates a new shaped recipe data instance.
     *
     * @see #shapedBuilder()
     */
    public static ShapedRecipeData createShaped(
        @NotNull
        String group,
        @NotNull
        CraftingCategory category,
        @NotNull
        ImmutableList<String> pattern,
        @NotNull
        ImmutableMap<Character, IngredientData> key,
        @NotNull
        ItemStack result,
        boolean showNotification
    ) {
        return ShapedRecipeData.of(group, category, pattern, key, result, showNotification);
    }

    /**
     * Creates a new shapeless recipe data instance.
     *
     * @see #shapelessBuilder()
     */
    public static ShapelessRecipeData of(
        @NotNull
        String group,
        @NotNull
        CraftingCategory category,
        @NotNull
        ImmutableList<IngredientData> ingredients,
        @NotNull
        ItemStack result
    ) {
        return ShapelessRecipeData.of(group, category,ingredients, result);
    }

    public static RecipeData<SingleRecipeInput, StonecuttingRecipe> createStonecutting(
        @NotNull
        String group,
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result
    ) {
        requireSpecified(group, "group");
        requireSpecified(ingredient, "ingredient");
        requireResult(result);

        return registries -> ingredient.createIngredient(registries)
            .map(resolvedIngredient -> new StonecuttingRecipe(group, resolvedIngredient, result));
    }

    public static RecipeData<SingleRecipeInput, StonecuttingRecipe> createStonecutting(
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result
    ) {
        return createStonecutting("", ingredient, result);
    }

    public static RecipeData<SingleRecipeInput, SmeltingRecipe> createSmelting(
        @NotNull
        String group,
        @NotNull
        CookingCategory category,
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result,
        float experience,
        int cookTime
    ) {
        return createCookingImpl(group, category, ingredient, result, experience, cookTime, SmeltingRecipe::new);
    }

    public static RecipeData<SingleRecipeInput, SmeltingRecipe> createSmelting(
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result
    ) {
        return createSmelting("", CookingCategory.MISC, ingredient, result, 0, 200);
    }

    public static RecipeData<SingleRecipeInput, BlastingRecipe> createBlasting(
        @NotNull
        String group,
        @NotNull
        CookingCategory category,
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result,
        float experience,
        int cookTime
    ) {
        return createCookingImpl(group, category, ingredient, result, experience, cookTime, BlastingRecipe::new);
    }

    public static RecipeData<SingleRecipeInput, BlastingRecipe> createBlasting(
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result
    ) {
        return createBlasting("", CookingCategory.MISC, ingredient, result, 0, 100);
    }

    public static RecipeData<SingleRecipeInput, SmokingRecipe> createSmoking(
        @NotNull
        String group,
        @NotNull
        CookingCategory category,
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result,
        float experience,
        int cookTime
    ) {
        return createCookingImpl(group, category, ingredient, result, experience, cookTime, SmokingRecipe::new);
    }

    public static RecipeData<SingleRecipeInput, SmokingRecipe> createSmoking(
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result
    ) {
        return createSmoking("", CookingCategory.MISC, ingredient, result, 0, 100);
    }

    public static RecipeData<SingleRecipeInput, CampfireCookingRecipe> createCampfire(
        @NotNull
        String group,
        @NotNull
        CookingCategory category,
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result,
        float experience,
        int cookTime
    ) {
        return createCookingImpl(group, category, ingredient, result, experience, cookTime, CampfireCookingRecipe::new);
    }

    public static RecipeData<SingleRecipeInput, CampfireCookingRecipe> createCampfire(
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result
    ) {
        return createCampfire("", CookingCategory.MISC, ingredient, result, 0, 100);
    }

    private static <R extends AbstractCookingRecipe> RecipeData<SingleRecipeInput, R> createCookingImpl(
        @NotNull
        String group,
        @NotNull
        CookingCategory category,
        @NotNull
        IngredientData ingredient,
        @NotNull
        ItemStack result,
        float experience,
        int cookTime,
        @NotNull
        CookingFactory<R> factory
    ) {
        requireSpecified(group, "group");
        requireSpecified(category, "category");
        requireSpecified(ingredient, "ingredient");
        requireResult(result);

        return registries -> ingredient.createIngredient(registries).map(resolvedIngredient ->
            factory.create(group, category, resolvedIngredient, result, experience, cookTime)
        );
    }

    @FunctionalInterface
    private interface CookingFactory<R extends AbstractCookingRecipe> {
        R create(
            String group,
            CookingCategory category,
            Ingredient ingredient,
            ItemStack result,
            float experience,
            int cookTime
        );
    }
}
