/**
 * This API is responsible for loading {@link net.minecraft.client.render.animation.Animation Animations} and {@link net.minecraft.client.model.TexturedModelData Models} from assets.
 * <p>
 * Animations are expected to be at {@code assets/[namespace]/animations/[path].json}, and are retrievable from an {@link org.quiltmc.qsl.rendering.entity_models.api.AnimationManager AnimationManager} with an identifier.
 * <p>
 * Models are loaded from {@code assets/[namespace]/models/entity/[path]/[layer_name].json}. Models can be automatically retrieved from the {@link net.minecraft.client.render.entity.model.EntityModelLoader EntityModelLoader} with the proper {@link net.minecraft.client.render.entity.model.EntityModelLayer EntityModelLayer}.
 */

package org.quiltmc.qsl.rendering.entity_models.api;
