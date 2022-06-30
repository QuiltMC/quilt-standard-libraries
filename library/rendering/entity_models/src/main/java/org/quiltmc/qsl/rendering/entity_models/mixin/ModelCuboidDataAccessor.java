package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.Vec3f;

@Mixin(ModelCuboidData.class)
public interface ModelCuboidDataAccessor {

    @Accessor("name")
    String name();

    @Accessor("offset")
    Vec3f offset();

    @Accessor("dimensions")
    Vec3f dimensions();

    @Accessor("extraSize")
    Dilation dilation();

    @Accessor("mirror")
    boolean mirror();

    @Accessor("textureUV")
    Vector2f uv();

    @Accessor("textureScale")
    Vector2f uvScale();

    @Invoker("<init>")
    static ModelCuboidData create(@Nullable String string, float f, float g, float h, float i, float j, float k, float l, float m, Dilation dilation, boolean bl, float n, float o){
        throw new AssertionError("Unreachable");
    }
}
