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
