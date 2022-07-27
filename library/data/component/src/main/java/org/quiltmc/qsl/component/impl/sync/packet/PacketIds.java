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

package org.quiltmc.qsl.component.impl.sync.packet;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.component.impl.CommonInitializer;

public final class PacketIds {
	public static final Identifier TYPES = CommonInitializer.id("types");
	public static final Identifier BLOCK_ENTITY_SYNC = CommonInitializer.id("block_entity");
	public static final Identifier ENTITY_SYNC = CommonInitializer.id("entity");
	public static final Identifier CHUNK_SYNC = CommonInitializer.id("chunk");
	public static final Identifier WORLD_SYNC = CommonInitializer.id("world");
	public static final Identifier LEVEL_SYNC = CommonInitializer.id("level");
}
