package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.quiltmc.qsl.rendering.entity_models.api.model.TypedModel;
import org.quiltmc.qsl.rendering.entity_models.api.model.ModelType;
import org.quiltmc.qsl.rendering.entity_models.api.model.ModelTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.model.TexturedModelData;

@Mixin(TexturedModelData.class)
public class TexturedModelDataMixin implements TypedModel {
	@Unique
	private ModelType quilt$modelType = ModelTypes.QUILT_MODEL;

	@Override
	public ModelType getType() {
		return quilt$modelType;
	}

	@Override
	public void setType(ModelType type) {
		quilt$modelType = type;
	}
}
