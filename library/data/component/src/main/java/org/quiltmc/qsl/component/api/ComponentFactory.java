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
	 * @param ctx The {@link ComponentCreationContext} that can be used by the component.
	 * @return A {@link T} instance.
	 */
	T create(ComponentCreationContext ctx);
}
