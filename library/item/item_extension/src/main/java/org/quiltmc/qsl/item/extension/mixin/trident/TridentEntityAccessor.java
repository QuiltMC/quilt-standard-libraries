package org.quiltmc.qsl.item.extension.mixin.trident;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;

@Mixin(TridentEntity.class)
public interface TridentEntityAccessor {
    @Accessor("tridentStack")
    ItemStack getTridentStack();

    @Accessor("tridentStack")
    void setTridentStack(ItemStack stack);
}
