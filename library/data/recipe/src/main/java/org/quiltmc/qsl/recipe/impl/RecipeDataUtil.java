package org.quiltmc.qsl.recipe.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class RecipeDataUtil {
    public static <T> T requireSpecified(T value, String name) {
        return requireNonNull(value, name + " must be specified");
    }

    public static ItemStack requireResult(ItemStack result) {
        requireSpecified(result, "result");
        if (result.isOf(Items.AIR)) {
            throw new IllegalArgumentException("result must not be air");
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("result must not be empty: " + result);
        }

        return result;
    }

    public static <T> Iterable<T> requireNonEmpty(Iterable<T> iterable, String name) {
        if (!requireNonNull(iterable).iterator().hasNext()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }

        return iterable;
    }
}
