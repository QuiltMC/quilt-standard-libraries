/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.signal_channel.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.signal_channel.api.SignalChannel;

import java.util.function.Consumer;

public sealed class SubscriptionImpl<T> implements SignalChannel.Subscription {
	protected final Consumer<T> consumer;
	private boolean isCancelled;

	@Nullable
	private final SubscriptionTracker tracker;

	@Nullable
	public SubscriptionTracker getTracker() {
		return tracker;
	}

	@Override
	public void cancel() {
		isCancelled = true;
	}

	public boolean shouldRemove() {
		return isCancelled || (tracker != null && tracker.shouldRemove());
	}

	public SubscriptionImpl(@NotNull Consumer<T> consumer, @Nullable SubscriptionTracker tracker) {
		this.consumer = consumer;
		this.tracker = tracker;
		this.isCancelled = false;
	}

	public static final class Unnamed<T> extends SubscriptionImpl<T> {
		public void accept(T signal) {
			consumer.accept(signal);
		}

		public Unnamed(@NotNull Consumer<T> consumer, @Nullable SubscriptionTracker tracker) {
			super(consumer, tracker);
		}
	}

	public static final class Named<T> extends SubscriptionImpl<T> {
		private final Codec<T> codec;

		public <S> void accept(S signal, Codec<S> sourceCodec) {
			if (sourceCodec.equals(codec)) {
				//don't convert direct subscriptions with same codec
				//noinspection unchecked
				consumer.accept((T) signal);
			} else {
				var convertedSignal = sourceCodec.encodeStart(JsonOps.INSTANCE, signal)
						.flatMap(e -> codec.parse(JsonOps.INSTANCE, e));
				//TODO there really has to be a better intermediate than JSON

				consumer.accept(convertedSignal.result().get()); //TODO actual error
			}
		}

		public Named(@NotNull Consumer<T> consumer, @Nullable SubscriptionTracker tracker, Codec<T> codec) {
			super(consumer, tracker);
			this.codec = codec;
		}
	}
}
