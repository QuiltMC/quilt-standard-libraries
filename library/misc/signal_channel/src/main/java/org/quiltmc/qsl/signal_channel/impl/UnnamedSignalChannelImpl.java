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

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.signal_channel.api.SignalChannel;

import java.util.function.Consumer;

public class UnnamedSignalChannelImpl<T> implements SignalChannel<T> {
	@Override
	@NotNull
	public SubscriptionBuilder subscribe(@NotNull Consumer<T> signalConsumer) {
		return new SubscriptionBuilderImpl.Unnamed<>(signalConsumer, this);
	}

	@Override
	public void emitInBox(T signal, @NotNull World world, @NotNull Box box) {
		WorldSubscriptionManager.managers.computeIfAbsent(
				world,
				WorldSubscriptionManager::new
		).getUnnamedSubscriptionsInBox(this, box).forEach(s -> s.accept(signal));
	}

	@Override
	public void emitInRange(T signal, @NotNull World world, @NotNull Vec3d origin, double range) {
		WorldSubscriptionManager.managers.computeIfAbsent(
				world,
				WorldSubscriptionManager::new
		).getUnnamedSubscriptionsInRange(this, origin, range).forEach(s -> s.accept(signal));

	}

	@Override
	public void emitWorldwide(T signal, @NotNull World world) {
		WorldSubscriptionManager.managers.computeIfAbsent(
				world,
				WorldSubscriptionManager::new
		).getUnnamedSubscriptions(this).forEach(s -> s.accept(signal));
	}

	@Override
	public void emitGlobal(T signal, @NotNull MinecraftServer server) {
		server.getWorlds().forEach(world -> emitWorldwide(signal, world));
	}
}
