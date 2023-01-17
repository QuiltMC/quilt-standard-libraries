package org.quiltmc.qsl.chat.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

import java.util.EnumSet;

public interface ChatEvent<H, R> {
	/**
	 * {@return the result of invoking this event, or null if there are no listeners}
	 */
	@Nullable R invoke(AbstractChatMessage<?> message);

	/**
	 * {@return the result of invoking this event, or ifNull if the result is null}
	 */
	R invokeOrElse(AbstractChatMessage<?> message, R ifNull);

	void register(EnumSet<QuiltMessageType> types, H handler);

	void register(@NotNull Identifier phaseIdentifier, EnumSet<QuiltMessageType> types, H handler);

	void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase);

	interface TypedChatApiHook<R> {
		EnumSet<QuiltMessageType> getMessageTypes();
		R handleMessage(AbstractChatMessage<?> message);
	}
}
