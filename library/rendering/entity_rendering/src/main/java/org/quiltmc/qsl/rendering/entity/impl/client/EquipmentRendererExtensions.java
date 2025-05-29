package org.quiltmc.qsl.rendering.entity.impl.client;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ApiStatus.Internal
@ClientOnly
public interface EquipmentRendererExtensions {
    void quilt$setArmorRenderLayerContext(ArmorRenderLayerContext context);

    void quilt$clearArmorRenderLayerContext();
}
