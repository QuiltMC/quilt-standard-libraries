package org.quiltmc.qsl.rendering.entity_models.mixin;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.TextureDimensions;
import net.minecraft.client.model.TexturedModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TexturedModelData.class)
public interface TexturedModelDataAccessor {
    @Accessor("data")
    ModelData root();

    @Accessor("dimensions")
    TextureDimensions texture();

    @Invoker("<init>")
    static TexturedModelData create(ModelData data, TextureDimensions dimensions) {
        throw new AssertionError("mixin broke");
    }
}
