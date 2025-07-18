/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.networking.api;

import io.netty.channel.ChannelFutureListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketSendListener;

/**
 * Utilities for working with {@link PacketSendListener}s.
 *
 * @see PacketSendListener
 */
public final class PacketSendListeners {
	/**
	 * {@return a packet send listener that executes the given runnable on success, or {@code null} if the runnable is {@code null}}
	 *
	 * @param runnable the runnable to run on success
	 */
	@Contract(value = "null -> null; !null -> new", pure = true)
	public static ChannelFutureListener ifSuccess(Runnable runnable) {
		if (runnable == null) return null;

		return (channelFuture) -> {
			if (channelFuture.isSuccess()) {
				runnable.run();
			}
		};
	}

	/**
	 * Combines two packet send listeners.
	 *
	 * @param first  the first packet send listener
	 * @param second the second packet send listener
	 * @return the combined packet send listeners, may be {@code null} if both the first and second packet send listeners are {@code null}
	 */
	@Contract(value = "_, null -> param1; null, _ -> param2; !null, !null -> new", pure = true)
	public static ChannelFutureListener union(@Nullable ChannelFutureListener first, @Nullable ChannelFutureListener second) {
		if (first == null && second == null) {
			return null;
		} else if (second == null) {
			return first;
		} else if (first == null) {
			return second;
		}

		return (channelFuture) -> {
			first.operationComplete(channelFuture);
			second.operationComplete(channelFuture);
		};
	}

	private PacketSendListeners() {
		throw new UnsupportedOperationException("PacketSendListeners only contains static-definitions.");
	}
}
