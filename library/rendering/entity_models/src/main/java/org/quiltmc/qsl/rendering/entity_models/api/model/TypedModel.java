package org.quiltmc.qsl.rendering.entity_models.api.model;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

import net.minecraft.client.model.TexturedModelData;

/**
 * An injected interface on {@link net.minecraft.client.model.TexturedModelData} to specify its type.
 * Defaults to {@link ModelTypes#QUILT_MODEL}.
 */
@InjectedInterface(TexturedModelData.class)
public interface TypedModel {
	ModelType getType();
	void setType(ModelType type);
}
