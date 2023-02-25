/*
 * Copyright 2022 QuiltMC
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
 * <h2>Block Extensions</h2>
 *
 * <p>
 * <h3>{@linkplain org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings Extended block settings}</h3>
 * <ul>
 *     <li>Provides additional methods to make creating blocks a bit easier.</li>
 *     <li>Also allows copying settings from built blocks, via
 *     {@link org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings#copyOf(net.minecraft.block.AbstractBlock)}.</li>
 *     <li>To use, simply replace {@link net.minecraft.block.AbstractBlock.Settings Block.Settings}{@code .of}
 *     with {@code QuiltBlockSettings.of}.</li>
 * </ul>
 *
 * <p>
 * <h3>{@linkplain org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder Extended material builder}</h3>
 * <ul>
 *     <li>Provides additional methods to make creating materials a bit easier.</li>
 *     <li>Also allows copying settings from existing materials, via
 *     {@link org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder#copyOf(net.minecraft.block.Material, net.minecraft.block.MapColor)}.</li>
 *     <li>Allows specifying that light passes through materials of this block via
 *     {@link org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder#lightPassesThrough()}.</li>
 *     <li>Allows setting default piston behavior via
 *     {@link org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder#pistonBehavior(net.minecraft.block.piston.PistonBehavior)}.</li>
 *     <li>To use, simply replace {@code new }{@link net.minecraft.block.Material.Builder}
 *     with {@code new QuiltMaterialBuilder}.</li>
 * </ul>
 */

package org.quiltmc.qsl.block.extensions.api;

// FIXME JD thinks QuiltMaterialBuilder#lightPassesThrough() is inaccessible because it's confusing it with
//  superclass method Material.Builder#lightPassesThrough() (which is package-private)
