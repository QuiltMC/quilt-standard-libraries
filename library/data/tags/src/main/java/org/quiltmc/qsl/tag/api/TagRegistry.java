/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.tag.api;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.GameEventTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

/**
 * Represents a tag registry.
 *
 * @param <T> the type of the content in the registered tags
 */
@ApiStatus.NonExtendable
public interface TagRegistry<T> {
	TagRegistry<Block> BLOCK = of(BlockTags::getTagGroup);
	TagRegistry<Item> ITEM = of(ItemTags::getTagGroup);
	TagRegistry<Fluid> FLUID = of(FluidTags::getTagGroup);
	TagRegistry<GameEvent> GAME_EVENT = of(GameEventTags::getTagGroup);
	TagRegistry<EntityType<?>> ENTITY_TYPE = of(EntityTypeTags::getTagGroup);
	TagRegistry<Biome> BIOME = of(Registry.BIOME_KEY, "tags/biomes");

	static <T> TagRegistry<T> of(Supplier<TagGroup<T>> tagGroupSupplier) {
		return TagRegistryImpl.of(tagGroupSupplier);
	}

	/**
	 * Create a new tag registry for the specified registry.
	 * <p>
	 * This method will fail if a tag registry for a given registry key is already registered
	 * under a different data type.
	 *
	 * @param registryKey the key of the registry
	 * @param dataType    the data type of this tag group, vanilla uses {@code tags/[plural]} format for built-in groups
	 */
	static <T> TagRegistry<T> of(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		return TagRegistryImpl.of(registryKey, dataType);
	}

	/**
	 * Creates a new identified optional tag.
	 * <p>
	 * May be required if something else registers one with the same identifier as required.
	 *
	 * @param id the identifier of the tag
	 * @return the identified tag
	 * @see #create(Identifier, TagType) create a tag with a specific type
	 */
	default Tag.Identified<T> create(Identifier id) {
		return this.create(id, TagType.OPTIONAL);
	}

	/**
	 * Creates a new identified tag.
	 * <p>
	 * If another tag with the same identifier is registered, it will not prevent the creation of a new tag instance,
	 * but tag types {@link TagType#SERVER_REQUIRED} and {@link TagType#REQUIRED} behavior will take priority,
	 * {@link TagType#CLIENT_ONLY} tags are entirely separate, and {@link TagType#CLIENT_FALLBACK} fallback content
	 * will not leak into tags of other types.
	 *
	 * @param id   the identifier of the tag
	 * @param type the type of the tag
	 * @return the identified tag
	 */
	Tag.Identified<T> create(Identifier id, TagType type);
}
