package org.quiltmc.qsl.key.binds.mixin.client.chords;

import com.mojang.blaze3d.platform.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InputUtil.Key.class)
public abstract class InputUtilKeyMixin implements Comparable<InputUtil.Key> {
    @Override
    public int compareTo(InputUtil.Key key) {
        return Integer.compare(this.getKeyCode(), key.getKeyCode());
    }

    @Shadow
    public abstract int getKeyCode();
}
