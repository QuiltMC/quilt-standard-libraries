package org.quiltmc.qsl.rendering.entity_models.api.animation;

import com.mojang.serialization.Codec;

import net.minecraft.client.render.animation.Animation;

/**
 * A type parameter that allows animations to be loaded in different ways.
 *
 * @param codec The codec to load an animation
 */
public record AnimationType(Codec<Animation> codec) {
}
