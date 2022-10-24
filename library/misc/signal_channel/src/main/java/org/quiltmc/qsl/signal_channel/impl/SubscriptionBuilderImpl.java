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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.signal_channel.api.SignalChannel;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public sealed abstract class SubscriptionBuilderImpl<T> implements SignalChannel.SubscriptionBuilder {
	protected final Consumer<T> consumer;

	public SubscriptionBuilderImpl(Consumer<T> consumer) {
		this.consumer = consumer;
	}

	protected abstract SignalChannel.Subscription buildAndAddSubscription(@Nullable SubscriptionTracker tracker, WorldSubscriptionManager manager);

	@Override
	@NotNull
	public SignalChannel.Subscription boundTo(@NotNull Entity entity) {
		return buildAndAddSubscription(
				new SubscriptionTracker.EntityTracker(new WeakReference<>(entity)),
				WorldSubscriptionManager.managers.computeIfAbsent(
						entity.getWorld(),
						WorldSubscriptionManager::new
				)
		);
	}

	@Override
	@NotNull
	public SignalChannel.Subscription boundTo(@NotNull BlockEntity blockEntity) {
		return buildAndAddSubscription(
				new SubscriptionTracker.BlockEntityTracker(new WeakReference<>(blockEntity)),
				WorldSubscriptionManager.managers.computeIfAbsent(
						blockEntity.getWorld(),
						WorldSubscriptionManager::new
				)
		);
	}

	@Override
	@NotNull
	public SignalChannel.Subscription atPosition(@NotNull World world, @NotNull Vec3d pos) {
		return buildAndAddSubscription(
				new SubscriptionTracker.FixedPositionTracker(pos),
				WorldSubscriptionManager.managers.computeIfAbsent(
						world,
						WorldSubscriptionManager::new
				)
		);
	}

	@Override
	@NotNull
	public SignalChannel.Subscription worldwide(@NotNull World world) {
		return buildAndAddSubscription(
				null,
				WorldSubscriptionManager.managers.computeIfAbsent(
						world,
						WorldSubscriptionManager::new
				)
		);
	}

	@Override
	@NotNull
	public SignalChannel.Subscription global(MinecraftServer server) {
		throw new NotImplementedException();
	}

	public static final class Named<T> extends SubscriptionBuilderImpl<T> {
		private final Identifier id;
		private final Codec<T> codec;

		public Named(Consumer<T> consumer, Identifier id, Codec<T> codec) {
			super(consumer);
			this.id = id;
			this.codec = codec;
		}

		@Override
		protected SignalChannel.Subscription buildAndAddSubscription(@Nullable SubscriptionTracker tracker, WorldSubscriptionManager manager) {
			var subscription = new SubscriptionImpl.Named<>(consumer, tracker, codec);
			manager.addNamedSubscription(id, subscription);
			return subscription;
		}
	}

	public static final class Unnamed<T> extends SubscriptionBuilderImpl<T> {
		private final SignalChannel<T> channel;

		public Unnamed(Consumer<T> consumer, SignalChannel<T> channel) {
			super(consumer);
			this.channel = channel;
		}

		@Override
		protected SignalChannel.Subscription buildAndAddSubscription(@Nullable SubscriptionTracker tracker, WorldSubscriptionManager manager) {
			var subscription = new SubscriptionImpl.Unnamed<>(consumer, tracker);
			manager.addUnnamedSubscription(channel, subscription);
			return subscription;
		}
	}
}
