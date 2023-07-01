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
