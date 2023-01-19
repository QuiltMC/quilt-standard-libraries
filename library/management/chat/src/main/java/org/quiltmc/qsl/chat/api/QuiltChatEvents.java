/*
 * Copyright 2023 QuiltMC
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
 * the message, in which case {@link #BEFORE_PROCESS} and {@link #AFTER_PROCESS} do not run.
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
	 * An event that allows you to cancel a message by returning true, or false to allow it to continue through.
	 */
	public static final ChatEvent<Cancel, Boolean> CANCEL = new ChatEventBooleanImpl<>(InternalChatEventCallbackConverters::cancelToHook);

	/**
	 * Before (usually) vanilla does any standard processing with this message. Mods may execute other behavior before or after this event.
	 */
	public static final ChatEvent<Listen, Void> BEFORE_PROCESS = new ChatEventImpl<>(false, InternalChatEventCallbackConverters::listenToHook);

	/**
	 * After (usually) vanilla does any standard processing with this message. Mods may execute other behavior before or after this event.
	 */
	public static final ChatEvent<Listen, Void> AFTER_PROCESS = new ChatEventImpl<>(false, InternalChatEventCallbackConverters::listenToHook);

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
		boolean shouldCancel(AbstractChatMessage<?> abstractMessage);
	}

	/**
	 * A {@link FunctionalInterface} that is used with both {@link #BEFORE_PROCESS} and {@link #AFTER_PROCESS} to listen for messages.
	 */
	@FunctionalInterface
	public interface Listen {
		void onMessage(AbstractChatMessage<?> abstractMessage);
	}
}
