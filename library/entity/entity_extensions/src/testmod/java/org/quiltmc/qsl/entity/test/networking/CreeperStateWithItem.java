package org.quiltmc.qsl.entity.test.networking;

import net.minecraft.item.ItemStack;

public interface CreeperStateWithItem {
    ItemStack quilt$getStack();

    void quilt$setStack(ItemStack stack);

    float quilt$getStackRotation();

    void quilt$setStackRotation(float rotation);
}
