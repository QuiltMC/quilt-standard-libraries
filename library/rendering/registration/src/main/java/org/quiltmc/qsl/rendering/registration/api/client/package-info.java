/**
 * <h2>Rendering Registration</h2>
 *
 * <p>
 * <h3>{@linkplain org.quiltmc.qsl.rendering.registration.api.client.BlockRenderLayerMap Block render layer map}</h3>
 * <ul>
 *     <li>Provides methods for setting the render layer of
 *     {@linkplain org.quiltmc.qsl.rendering.registration.api.client.BlockRenderLayerMap#put(net.minecraft.client.render.RenderLayer, net.minecraft.block.Block...) blocks}
 *     and {@linkplain org.quiltmc.qsl.rendering.registration.api.client.BlockRenderLayerMap#put(net.minecraft.client.render.RenderLayer, net.minecraft.fluid.Fluid...) fluids}.</li>
 * </ul>
 *
 * <p>
 * <h3>{@linkplain org.quiltmc.qsl.rendering.registration.api.client.DynamicItemRenderer Dynamic item rendering}</h3>
 * <ul>
 *     <li>Allows items to define dynamic rendering behavior.</li>
 * </ul>
 *
 * <p>
 * <h3>{@linkplain org.quiltmc.qsl.rendering.registration.api.client.ArmorRenderer Custom armor rendering}</h3>
 * <ul>
 *     <li>Allows armor items to define custom rendering behavior.</li>
 * </ul>
 *
 * <p>
 * <h3>{@linkplain org.quiltmc.qsl.rendering.registration.api.client.EntityModelLayerRegistry Entity model layer registry}</h3>
 * <ul>
 *     <li>Allows registering {@linkplain net.minecraft.client.render.entity.model.EntityModelLayer entity model layers} and mapping them to
 *     {@linkplain net.minecraft.client.model.TexturedModelData textured model data}.</li>
 * </ul>
 */

package org.quiltmc.qsl.rendering.registration.api.client;
