package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.util.math.Vector2f;

@Mixin(Vector2f.class)
public abstract class Vector2fMixin {
    @Shadow
    public abstract float getX();

    @Shadow
    public abstract float getY();

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector2f vec) {
            return this.getX() == vec.getX() && this.getY() == vec.getY();
        }
        return super.equals(o);
    }
}
