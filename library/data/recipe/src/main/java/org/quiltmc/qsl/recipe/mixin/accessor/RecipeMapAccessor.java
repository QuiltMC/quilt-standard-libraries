package org.quiltmc.qsl.recipe.mixin.accessor;

import com.google.common.collect.Multimap;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeMap;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RecipeMap.class)
public interface RecipeMapAccessor {
    @Accessor("byType")
    Multimap<RecipeType<?>, RecipeHolder<?>> quilt$getByType();

    @Accessor("byKey")
    Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> quilt$getByKey();

    @Invoker("<init>")
    static RecipeMap quilt$create(
        Multimap<RecipeType<?>, RecipeHolder<?>> byType,
        Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> byKey
    ) {
        throw new AssertionError("Dummy method called");
    }
}
