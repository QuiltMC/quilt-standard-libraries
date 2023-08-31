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

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

/**
 * An interface for chat events. This copies much of its behavior from {@link Event}, but in its own interface.
 *
 * @param <C> the type of the callback for this event to accept in registration
 * @param <R> the return type of invoking this event
 */
public interface ChatEvent<C, R> {
	/**
	 * Invokes the event with the provided message and returns the result or {@code null}. A {@code null} result is usually the result of no callbacks
	 * being attached to this event, but may occur for any other reason depending on the implementation.
	 *
	 * @param message the message for the event to process
	 * @return the result of invoking this event, or {@code null}
	 */
	@Nullable R invoke(@NotNull AbstractChatMessage<?> message);

	/**
	 * Invokes the event with the provided message, but replaces the result with {@code ifNull} if the result would have been {@code null}.
	 *
	 * @param message the message for the event to process
	 * @param ifNull a value that should be returned instead of {@code null}
	 * @return the result of invoking this event, or {@code ifNull} if the result would be {@code null}
	 */
	R invokeOrElse(@NotNull AbstractChatMessage<?> message, @NotNull R ifNull);

	/**
	 * Register a callback for the event.
	 *
	 * @param types an {@link EnumSet} of {@link QuiltMessageType} to determine what chat events to receive
	 * @param callback the callback to register
	 * @see #register(Identifier, EnumSet, Object)
	 */
	void register(@NotNull EnumSet<QuiltMessageType> types, @NotNull C callback);


	/**
	 * Registers a callback to a specific phase of the event.
	 *
	 * @param phaseIdentifier the phase identifier
	 * @param callback the callback to register
	 */
	void register(@NotNull Identifier phaseIdentifier, @NotNull EnumSet<QuiltMessageType> types, @NotNull C callback);

	/**
	 * Request that callbacks registered for one phase be executed before callbacks registered for another phase.
	 * <p>
	 * Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * @param firstPhase  the identifier of the phase that should run before the other. It will be created if it didn't exist yet
	 * @param secondPhase the identifier of the phase that should run after the other. It will be created if it didn't exist yet
	 */
	void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase);

	/**
	 * The common interface describing a chat message hook. This should almost never be implemented manually, instead
	 * relying on various converters for each event to convert a {@link FunctionalInterface} into one of these.
	 *
	 * @param <R> the return type of handling a message with this hook
	 */
	interface TypedChatApiHook<R> {
		EnumSet<QuiltMessageType> getMessageTypes();
		R handleMessage(@NotNull AbstractChatMessage<?> message);
	}
}
