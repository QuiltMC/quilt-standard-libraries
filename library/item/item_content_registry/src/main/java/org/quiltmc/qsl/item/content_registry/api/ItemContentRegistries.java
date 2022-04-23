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

package org.quiltmc.qsl.item.content_registry.api;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentHolder;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemContentRegistries {
	public static final RegistryEntryAttachment<Item, Integer> FUEL_TIME = RegistryEntryAttachment.intBuilder(Registry.ITEM, new Identifier("quilt_item_content_registry", "fuel_time")).build();
	public static final RegistryEntryAttachment<Item, Float> COMPOST_CHANCE = RegistryEntryAttachment.floatBuilder(Registry.ITEM, new Identifier("quilt_item_content_registry", "compost_chance")).build();

	public static <T> void addItem(RegistryEntryAttachment<Item, T> attachment, Item item, T value) {
		RegistryEntryAttachmentHolder<Item> holder = RegistryEntryAttachmentHolder.getBuiltin(Registry.ITEM);
		holder.putValue(attachment, item, value);
	}

	public static <T> void addTag(RegistryEntryAttachment<Item, T> attachment, TagKey<Item> tag, T value) {
		RegistryEntryAttachmentHolder<Item> holder = RegistryEntryAttachmentHolder.getBuiltin(Registry.ITEM);
		holder.putValueTag(attachment, tag, value);
	}
}

