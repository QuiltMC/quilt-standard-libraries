/*
 * Copyright 2022 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * <h2>The Quilt Tooltip API</h2>
 *
 * <p>
 * <h3>What are the tooltip APIs?</h3>
 * With Minecraft 1.17, a new system has been introduced through the {@link net.minecraft.item.BundleItem}: custom tooltip components.
 * <p>
 * Those tooltip components are created using mainly two classes:
 * <ul>
 *     <li>{@link net.minecraft.client.item.TooltipData}, which holds information about the tooltip, exists on both sides;</li>
 *     <li>{@link net.minecraft.client.gui.tooltip.TooltipComponent}, which does the rendering of the tooltip, only exists on the client.</li>
 * </ul>
 * An item can return a custom tooltip data by overriding the method {@link net.minecraft.item.Item#getTooltipData(net.minecraft.item.ItemStack)}.
 * However, there is no method in Vanilla to convert custom tooltip data into a component.
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
 *
 * <p>
 * <h3>Text-based tooltips</h3>
 * When hovering an item, the client can display text about it in a tooltip.
 * It can be appended to per-item through
 * {@link net.minecraft.item.Item#appendTooltip(net.minecraft.item.ItemStack, net.minecraft.world.World, java.util.List, net.minecraft.client.item.TooltipContext)},
 * but what if you want to append new lines on an existing item?
 * <p>
 * That's the job of {@link org.quiltmc.qsl.tooltip.api.client.ItemTooltipCallback}.
 */

package org.quiltmc.qsl.tooltip.api;
