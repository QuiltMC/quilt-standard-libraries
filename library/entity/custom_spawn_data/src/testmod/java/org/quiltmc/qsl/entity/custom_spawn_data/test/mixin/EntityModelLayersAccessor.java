package org.quiltmc.qsl.entity.custom_spawn_data.test.mixin;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(EntityModelLayers.class)
public interface EntityModelLayersAccessor {
	@Accessor("LAYERS")
	static Set<EntityModelLayer> quilt$LAYERS() {
		throw new IllegalStateException("mixin failed");
	}

}
