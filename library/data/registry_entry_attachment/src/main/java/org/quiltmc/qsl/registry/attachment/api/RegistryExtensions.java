/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.api;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Extensions for working with {@link Registry}s.
 */
public final class RegistryExtensions {
	/**
	 * Utility method to register an entry and associate a value to it in an attachment.
	 *
	 * @param registry target registry
	 * @param id       entry identifier
	 * @param entry    entry to register
	 * @param attach1  attachment
	 * @param value1   value to associate to entry in attachment
	 * @param <R>      type of the entries in the registry
	 * @param <T>      type of the entry we're currently registering (may be a subclass of {@code R})
	 * @param <V1>     type of the attached value
	 * @return the newly registered entry
	 */
	public static <R, T extends R, V1> T register(Registry<R> registry, Identifier id, T entry,
			RegistryEntryAttachment<R, V1> attach1, V1 value1) {
		Registry.register(registry, id, entry);
		attach1.put(entry, value1);
		return entry;
	}

	/**
	 * Utility method to register an entry and associate values to it in multiple attachments.
	 *
	 * @param registry target registry
	 * @param id       entry identifier
	 * @param entry    entry to register
	 * @param attach1  first attachment
	 * @param value1   value to associate to entry in first attachment
	 * @param attach2  second attachment
	 * @param value2   value to associate to entry in second attachment
	 * @param <R>      type of the entries in the registry
	 * @param <T>      type of the entry we're currently registering (may be a subclass of {@code R})
	 * @param <V1>     type of the first attached value
	 * @param <V2>     type of the second attached value
	 * @return the newly registered entry
	 */
	public static <R, T extends R, V1, V2> T register(Registry<R> registry, Identifier id, T entry,
			RegistryEntryAttachment<R, V1> attach1, V1 value1,
			RegistryEntryAttachment<R, V2> attach2, V2 value2) {
		Registry.register(registry, id, entry);
		attach1.put(entry, value1);
		attach2.put(entry, value2);
		return entry;
	}

	/**
	 * Utility method to register an entry and associate values to it in multiple attachments.
	 *
	 * @param registry target registry
	 * @param id       entry identifier
	 * @param entry    entry to register
	 * @param attach1  first attachment
	 * @param value1   value to associate to entry in first attachment
	 * @param attach2  second attachment
	 * @param value2   value to associate to entry in second attachment
	 * @param attach3  third attachment
	 * @param value3   value to associate to entry in third attachment
	 * @param <R>      type of the entries in the registry
	 * @param <T>      type of the entry we're currently registering (may be a subclass of {@code R})
	 * @param <V1>     type of the first attached value
	 * @param <V2>     type of the second attached value
	 * @param <V3>     type of the third attached value
	 * @return the newly registered entry
	 */
	public static <R, T extends R, V1, V2, V3> T register(Registry<R> registry, Identifier id, T entry,
			RegistryEntryAttachment<R, V1> attach1, V1 value1,
			RegistryEntryAttachment<R, V2> attach2, V2 value2,
			RegistryEntryAttachment<R, V3> attach3, V3 value3) {
		Registry.register(registry, id, entry);
		attach1.put(entry, value1);
		attach2.put(entry, value2);
		attach3.put(entry, value3);
		return entry;
	}

	/**
	 * Utility method to register an entry and associate values to it in multiple attachments.
	 *
	 * @param registry target registry
	 * @param id       entry identifier
	 * @param entry    entry to register
	 * @param attach1  first attachment
	 * @param value1   value to associate to entry in first attachment
	 * @param attach2  second attachment
	 * @param value2   value to associate to entry in second attachment
	 * @param attach3  third attachment
	 * @param value3   value to associate to entry in third attachment
	 * @param attach4  fourth attachment
	 * @param value4   value to associate to entry in fourth attachment
	 * @param <R>      type of the entries in the registry
	 * @param <T>      type of the entry we're currently registering (may be a subclass of {@code R})
	 * @param <V1>     type of the first attached value
	 * @param <V2>     type of the second attached value
	 * @param <V3>     type of the third attached value
	 * @param <V4>     type of the fourth attached value
	 * @return the newly registered entry
	 */
	public static <R, T extends R, V1, V2, V3, V4> T register(Registry<R> registry, Identifier id, T entry,
			RegistryEntryAttachment<R, V1> attach1, V1 value1,
			RegistryEntryAttachment<R, V2> attach2, V2 value2,
			RegistryEntryAttachment<R, V3> attach3, V3 value3,
			RegistryEntryAttachment<R, V4> attach4, V4 value4) {
		Registry.register(registry, id, entry);
		attach1.put(entry, value1);
		attach2.put(entry, value2);
		attach3.put(entry, value3);
		attach4.put(entry, value4);
		return entry;
	}
}
