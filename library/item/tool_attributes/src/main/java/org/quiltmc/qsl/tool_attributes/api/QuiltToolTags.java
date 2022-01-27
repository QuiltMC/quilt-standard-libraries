/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tool_attributes.api;

import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.tag.api.TagRegistry;

/**
 * This class contains the QuiltToolTags. These tags can be assigned to modded tools, to allow them to fulfill their desired function.
 */
public class QuiltToolTags {

	public static final Tag<Item> AXES = register("axes");
	public static final Tag<Item> HOES = register("hoes");
	public static final Tag<Item> PICKAXES = register("pickaxes");
	public static final Tag<Item> SHOVELS = register("shovels");
	public static final Tag<Item> SWORDS = register("swords");
	public static final Tag<Item> SHEARS = register("shears");

	private QuiltToolTags() { }

	private static Tag<Item> register(String id) {
		return TagRegistry.ITEM.create(new Identifier("quilt", id));
	}
}
