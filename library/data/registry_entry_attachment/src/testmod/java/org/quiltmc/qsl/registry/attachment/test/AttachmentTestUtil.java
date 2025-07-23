/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.test;

import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;

public final class AttachmentTestUtil {
	private AttachmentTestUtil() {
		throw new UnsupportedOperationException(
			AttachmentTestUtil.class.getSimpleName() + " contains only static members"
		);
	}

	public static final String NAMESPACE = "quilt";

	public static Identifier createId(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static RegistryKey<Item> createItemKey(String path) {
		return RegistryKey.of(RegistryKeys.ITEM, createId(path));
	}

	public static <I extends Item> I registerItem(String path, Function<Item.Settings, I> factory) {
		final RegistryKey<Item> key = createItemKey(path);
		return Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().key(key)));
	}

	public static <I extends Item, V> I registerItemWithExtension(
			String path, Function<Item.Settings, I> factory, RegistryEntryAttachment<Item, V> attachment, V value
	) {
		final RegistryKey<Item> key = createItemKey(path);
		return RegistryExtensions.register(
			Registries.ITEM, key.getValue(),
			factory.apply(new Item.Settings().key(key)),
			attachment, value
		);
	}
}
