/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.chat.api;

import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;
import org.quiltmc.qsl.chat.impl.ChatEventBooleanImpl;
import org.quiltmc.qsl.chat.impl.InternalChatEventCallbackConverters;
import org.quiltmc.qsl.chat.impl.ChatEventImpl;

/**
 * Events for modifying, canceling, and listening for various chat messages.
 * Events are always executed in the order {@link #MODIFY} -> {@link #CANCEL} -> {@link #BEFORE_PROCESS} -> {@link #AFTER_PROCESS}, unless a mod cancels
 * the message, in which case {@link #CANCELLED} is invoked instead of {@link #BEFORE_PROCESS} and {@link #AFTER_PROCESS}.
 * <p>
 * When listening, you will only receive messages that match the provided types of your listener. If you do not provide any of a certain meta message type,
 * then any messages for that meta type will be passed along as long as they match your other specified types.
 */
public final class QuiltChatEvents {
	private QuiltChatEvents() {}

	/**
	 * An event that allows you to modify a message before further processing by returning a new one to replace it.
	 * The usage of `withX` methods is recommended.
	 */
	public static final ChatEvent<Modify, AbstractChatMessage<?>> MODIFY = new ChatEventImpl<>(true, InternalChatEventCallbackConverters::modifyToHook);

	/**
	 * An event that allows you to cancel a message by returning {@code true}, or {@code false} to allow it to continue through.
	 *
	 * @see #CANCELLED
	 */
	public static final ChatEvent<Cancel, Boolean> CANCEL = new ChatEventBooleanImpl<>(InternalChatEventCallbackConverters::cancelToHook);

	/**
	 * An event that allows you to listen for messages that have been cancelled.
	 *
	 * @see #CANCEL
	 */
	public static final ChatEvent<Cancelled, Void> CANCELLED = new ChatEventImpl<>(false, InternalChatEventCallbackConverters::cancelledToHook);

	/**
	 * Before (usually) vanilla does any standard processing with this message. Mods may execute other behavior before or after this event.
	 */
	public static final ChatEvent<Before, Void> BEFORE_PROCESS = new ChatEventImpl<>(false, InternalChatEventCallbackConverters::beforeToHook);

	/**
	 * After (usually) vanilla does any standard processing with this message. Mods may execute other behavior before or after this event.
	 */
	public static final ChatEvent<After, Void> AFTER_PROCESS = new ChatEventImpl<>(false, InternalChatEventCallbackConverters::afterToHook);

	/**
	 * A {@link FunctionalInterface} that is used with {@link #MODIFY} to modify messages.
	 */
	@FunctionalInterface
	public interface Modify {
		AbstractChatMessage<?> modifyMessage(AbstractChatMessage<?> abstractMessage);
	}

	/**
	 * A {@link FunctionalInterface} that is used with {@link #CANCEL} to cancel messages.
	 */
	@FunctionalInterface
	public interface Cancel {
		boolean shouldCancelMessage(AbstractChatMessage<?> abstractMessage);
	}

	/**
	 * A {@link FunctionalInterface} that is used with {@link #CANCELLED}to listen for cancelled messages.
	 */
	@FunctionalInterface
	public interface Cancelled {
		void onMessageCancelled(AbstractChatMessage<?> abstractMessage);
	}

	/**
	 * A {@link FunctionalInterface} that is used with {@link #BEFORE_PROCESS} to listen for before messages.
	 */
	@FunctionalInterface
	public interface Before {
		void beforeMessage(AbstractChatMessage<?> abstractMessage);
	}

	/**
	 * A {@link FunctionalInterface} that is used with {@link #AFTER_PROCESS} to listen for after messages.
	 */
	@FunctionalInterface
	public interface After {
		void afterMessage(AbstractChatMessage<?> abstractMessage);
	}
}
