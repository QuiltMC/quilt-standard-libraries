/**
 * <h2>The Quilt Tooltip API</h2>
 *
 * <p>
 * <h3>What are the tooltip APIs?</h3>
 * With Minecraft 1.17, a new system has been introduced through the {@link net.minecraft.item.BundleItem}: custom tooltip components.
 * <p>
 * Those tooltip components are done using mainly two classes:
 * <ul>
 *     <li>{@link net.minecraft.client.item.TooltipData} which will hold information about the tooltip, exist on both sides;</li>
 *     <li>{@link net.minecraft.client.gui.tooltip.TooltipComponent} which will do the rendering of the tooltip, only exist on the client.</li>
 * </ul>
 * An item can return a custom tooltip data with its method {@link net.minecraft.item.Item#getTooltipData(net.minecraft.item.ItemStack)}.
 * The issue is the conversion from the tooltip data to the tooltip component, the tooltip data is not aware of a conversion mechanism.
 * <p>
 * Thus this API introduces:
 * <ul>
 *     <li>
 *         {@link org.quiltmc.qsl.tooltip.api.ConvertibleTooltipData} to provide the missing conversion mechanism directly in the tooltip data,
 *         please read its documentation carefully;
 *     </li>
 *     <li>
 *         {@link org.quiltmc.qsl.tooltip.api.client.TooltipComponentCallback} on the client,
 *         an event which is triggered when a tooltip data tries to be converted to a tooltip component.
 *     </li>
 * </ul>
 */

package org.quiltmc.qsl.tooltip.api;
