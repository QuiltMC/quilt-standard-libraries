package org.quiltmc.qsl.component.api;

/**
 * Class meant to provide a component using the provided {@link ComponentCreationContext} argument.
 *
 * @param <T> The type of the returned component.
 * @author 0xJoeMama
 */
@FunctionalInterface
public interface ComponentFactory<T> {
	/**
	 * @param operations The {@link ComponentCreationContext} that the {@link Component} may use.
	 * @return A {@link T} instance.
	 */
	T create(ComponentCreationContext operations);
}
