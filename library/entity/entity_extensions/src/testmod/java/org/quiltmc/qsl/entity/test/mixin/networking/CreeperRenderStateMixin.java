package org.quiltmc.qsl.entity.test.mixin.networking;

import org.quiltmc.qsl.entity.test.networking.CreeperStateWithItem;

import net.minecraft.client.render.entity.state.CreeperRenderState;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreeperRenderState.class)
abstract class CreeperRenderStateMixin implements CreeperStateWithItem {
    @Unique
    private ItemStack stackToDrop;

    @Unique
    private float stackRotation;

    @Override
    public ItemStack quilt$getStack() {
        return this.stackToDrop;
    }

    @Override
    public void quilt$setStack(ItemStack stack) {
        this.stackToDrop = stack;
    }

    @Override
    public float quilt$getStackRotation() {
        return this.stackRotation;
    }

    @Override
    public void quilt$setStackRotation(float rotation) {
        this.stackRotation = rotation;
    }
}
