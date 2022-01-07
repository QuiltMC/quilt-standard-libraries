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
