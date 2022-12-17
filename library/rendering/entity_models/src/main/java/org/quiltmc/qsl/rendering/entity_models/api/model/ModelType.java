package org.quiltmc.qsl.rendering.entity_models.api.model;

import com.mojang.serialization.Codec;

import net.minecraft.client.model.TexturedModelData;

/**
 * A type parameter that allows models to be loaded in different ways.
 *
 * @param codec The codec to load a model
 */
public record ModelType(Codec<TexturedModelData> codec) {
}
