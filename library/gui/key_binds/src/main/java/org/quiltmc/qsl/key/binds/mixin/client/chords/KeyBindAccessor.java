package org.quiltmc.qsl.key.binds.mixin.client.chords;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.option.KeyBind;

@Mixin(KeyBind.class)
public interface KeyBindAccessor {
    @Accessor
    int getTimesPressed();
    
    @Accessor
    void setTimesPressed(int timesPressed);
}
