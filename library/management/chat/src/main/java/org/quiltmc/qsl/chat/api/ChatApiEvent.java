package org.quiltmc.qsl.chat.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.api.types.ImmutableAbstractMessage;

import java.util.EnumSet;
import java.util.function.Function;

// Cant extend event because the constructor is private, gotta reproduce some of the API surface
public class ChatApiEvent<R> {
	private final Event<ChatApiHook<@Nullable R>> backingEvent = Event.create(ChatApiHook.class, hooks -> new ChatApiHook<>() {
		@Override
		public EnumSet<QuiltMessageType> getMessageTypes() {
			return EnumSet.allOf(QuiltMessageType.class);
		}

		@Override
		public R handleMessage(ImmutableAbstractMessage<?, ?> message) {
			R result = null;

			for (var hook : hooks) {
				if (shouldPassOnMessageToHook(message.getTypes(), hook.getMessageTypes())) {
					result = hook.handleMessage(message);
				}
			}

			return result;
		}
	});

	private boolean shouldPassOnMessageToHook(EnumSet<QuiltMessageType> messageTypes, EnumSet<QuiltMessageType> hookTypes) {
		// For every message type
		for (var messageType : messageTypes) {
			// If the hook isn't looking for it
			if (!hookTypes.contains(messageType)) {
				// If it doesn't match the complex rule
				if (!matchesMetaTypeRule(messageType.metaType, hookTypes)) {
					// Not a match
					return false;
				}
			}
		}

		// All message types are wanted, pass it on
		return true;
	}

	private boolean matchesMetaTypeRule(QuiltMessageType.QuiltMetaMessageType metaType, EnumSet<QuiltMessageType> hookTypes) {
		// For every type the hook is looking for
		for (var hookType : hookTypes) {
			// Check if they have the same meta type
			if (hookType.metaType == metaType) {
				// If so, don't pass it on
				// We eliminated equal type previously, so this is only same-meta-different-type
				return false;
			}
		}

		return true;
	}

	public R invoke(ImmutableAbstractMessage<?, ?> message) {
		return backingEvent.invoker().handleMessage(message);
	}

	public void register(EnumSet<QuiltMessageType> types, Function<ImmutableAbstractMessage<?, ?>, R> handler) {
		backingEvent.register(new ChatApiHook<R>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public R handleMessage(ImmutableAbstractMessage<?, ?> message) {
				return handler.apply(message);
			}
		});
	}

	public void register(@NotNull Identifier phaseIdentifier, EnumSet<QuiltMessageType> types, Function<ImmutableAbstractMessage<?, ?>, R> handler) {
		backingEvent.register(phaseIdentifier, new ChatApiHook<R>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public R handleMessage(ImmutableAbstractMessage<?, ?> message) {
				return handler.apply(message);
			}
		});
	}

	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		backingEvent.addPhaseOrdering(firstPhase, secondPhase);
	}

	private interface ChatApiHook<R> {
		EnumSet<QuiltMessageType> getMessageTypes();
		R handleMessage(ImmutableAbstractMessage<?, ?> message);
	}
}
