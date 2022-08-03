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

package org.quiltmc.qsl.registry.impl.sync;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;

/**
 * Registry flags used to determine how registry (entries) should be handled by sync.
 */
@ApiStatus.Internal
public enum RegistryFlag {
	OPTIONAL,
	SKIP;

	public static boolean isOptional(byte flag) {
		return (flag >>> OPTIONAL.ordinal() & 0x1) == 1;
	}

	public static boolean isSkipped(byte flag) {
		return (flag >>> SKIP.ordinal() & 0x1) == 1;
	}

	public static void setRegistry(SimpleRegistry<?> registry, RegistryFlag flag) {
		SynchronizedRegistry.as(registry).quilt$setRegistryFlag(flag);
	}

	@SuppressWarnings("unchecked")
	public static void setEntry(SimpleRegistry<?> registry, Identifier identifier, RegistryFlag flag) {
		SynchronizedRegistry.as((SimpleRegistry<Object>) registry).quilt$setEntryFlag(registry.get(identifier), flag);
	}

	public static <T> void setEntry(SimpleRegistry<T> registry, T entry, RegistryFlag flag) {
		SynchronizedRegistry.as(registry).quilt$setEntryFlag(entry, flag);
	}
}
