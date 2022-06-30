package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.Dilation;

@Mixin(Dilation.class)
public interface DilationAccessor {
    @Accessor("radiusX")
    float radiusX();

    @Accessor("radiusY")
    float radiusY();

    @Accessor("radiusZ")
    float radiusZ();
}
