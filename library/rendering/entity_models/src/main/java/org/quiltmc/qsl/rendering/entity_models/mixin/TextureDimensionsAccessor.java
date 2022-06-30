package org.quiltmc.qsl.rendering.entity_models.mixin;

import net.minecraft.client.model.TextureDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureDimensions.class)
public interface TextureDimensionsAccessor {
    @Accessor("width")
    int width();

    @Accessor("height")
    int height();
}
