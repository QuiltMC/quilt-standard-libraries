package org.quiltmc.qsl.rendering.entity_models.mixin;

import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(ModelPartData.class)
public interface ModelPartDataAccessor {
    @Accessor("cuboidData")
    List<ModelCuboidData> cuboids();

    @Accessor("rotationData")
    ModelTransform transform();

    @Accessor("children")
    Map<String, ModelPartData> children();

    @Invoker("<init>")
    static ModelPartData create(List<ModelCuboidData> cuboids, ModelTransform rotation) {
        throw new AssertionError("mixin broke");
    }
}
