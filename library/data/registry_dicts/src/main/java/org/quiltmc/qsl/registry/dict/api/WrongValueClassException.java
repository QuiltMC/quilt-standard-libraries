/*
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

package org.quiltmc.qsl.registry.dict.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Thrown by {@link RegistryDict#get(Registry, Identifier, Class)}, should a dictionary with a matching ID be found
 * but have a different value class than expected.
 */
public final class WrongValueClassException extends Exception {
	private final Class<?> expectedClass, actualClass;

	public WrongValueClassException(Registry<?> registry, Identifier dictId, Class<?> expectedClass, Class<?> actualClass) {
		super(("Found dictionary with ID \"%s\" for registry \"%s\", " +
				"but it has wrong value class (expected %s, got %s)")
				.formatted(dictId, registry.getKey().getValue(), expectedClass, actualClass));
		this.expectedClass = expectedClass;
		this.actualClass = actualClass;
	}

	public Class<?> getExpectedClass() {
		return expectedClass;
	}

	public Class<?> getActualClass() {
		return actualClass;
	}
}
