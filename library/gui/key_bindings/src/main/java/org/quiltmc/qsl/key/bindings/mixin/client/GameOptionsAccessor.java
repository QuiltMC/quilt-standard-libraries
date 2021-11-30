package org.quiltmc.qsl.key.bindings.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

@Mixin(GameOptions.class)
public interface GameOptionsAccessor {
    @Accessor
    void setKeysAll(KeyBinding[] keysAll);
}
