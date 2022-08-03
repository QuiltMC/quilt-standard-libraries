/**
 * <h2>The Block Entity APIs.</h2>
 *
 * <p>
 * This module provides {@linkplain net.minecraft.block.entity.BlockEntity BlockEntity}-related APIs, such as:
 * <ul>
 *     <li>
 *         {@linkplain org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder QuiltBLockEntityTypeBuilder} an extended BlockEntityTypeBuilder,
 *         which allows to add more supported blocks before building through separate calls.
 *     </li>
 *     <li>
 *         {@linkplain org.quiltmc.qsl.block.entity.api.QuiltBlockEntityType QuiltBlockEntityType} an injected interface into
 *         {@linkplain net.minecraft.block.entity.BlockEntityType BlockEntityType} which allows to add more supported blocks post-registration of the
 *         type.
 *     </li>
 *     <li>
 *         {@linkplain org.quiltmc.qsl.block.entity.api.QuiltBlockEntity QuiltBlockEntity} which is an interface that extends the functionality of the
 *         {@linkplain net.minecraft.block.entity.BlockEntity BlockEntity} class, to use it simply implement it into your block entity and access to its
 *         features will be granted.
 *     </li>
 * </ul>
 *
 * @see org.quiltmc.qsl.block.entity.api.QuiltBlockEntity
 * @see org.quiltmc.qsl.block.entity.api.QuiltBlockEntityType
 * @see org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder
 */

package org.quiltmc.qsl.block.entity.api;
