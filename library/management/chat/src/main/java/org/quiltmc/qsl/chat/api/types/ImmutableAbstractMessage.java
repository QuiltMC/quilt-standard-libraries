package org.quiltmc.qsl.chat.api.types;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;

import java.util.EnumSet;

/**
 * @param <T> The type of the immutable type
 * @param <S> The type of the packet form of this class
 */
public abstract class ImmutableAbstractMessage<T extends ImmutableAbstractMessage<T, S>, S> {
	// These are mostly annotated as @NotNull for dev productivity, I get nice warnings about
	// What parts of the subclasses I need to implement
	abstract public @NotNull EnumSet<QuiltMessageType> getTypes();

	abstract public @NotNull T immutableCopy();

	abstract public @NotNull S packet();
}
