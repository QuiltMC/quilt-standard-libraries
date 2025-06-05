package org.quiltmc.qsl.recipe.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Collection;

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

    public static <T, I extends Iterable<T>> I requireNonEmpty(I iterable, String name) {
        final boolean empty = iterable instanceof Collection<?> collection ? collection.isEmpty()
            : !requireSpecified(iterable, name).iterator().hasNext();

        if (empty) {
            throw new IllegalArgumentException(name + " must not be empty");
        }

        return iterable;
    }
}
