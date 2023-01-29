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

package org.quiltmc.qsl.signal_channel.api;

import com.mojang.serialization.Codec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.signal_channel.impl.NamedSignalChannelImpl;
import org.quiltmc.qsl.signal_channel.impl.UnnamedSignalChannelImpl;

import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface SignalChannel<T> {
	@NotNull
	static <T> SignalChannel<T> createUnnamed() {
		return new UnnamedSignalChannelImpl<>();
	}

	@NotNull
	static <T> SignalChannel<T> getOrCreateNamed(@NotNull Identifier id, @NotNull Codec<T> codec) {
		return new NamedSignalChannelImpl<>(id, codec);
	}

	@NotNull
	SubscriptionBuilder subscribe(@NotNull Consumer<T> signalConsumer);

	void emitInBox(T signal, @NotNull World world, @NotNull Box box);

	void emitInRange(T signal, @NotNull World world, @NotNull Vec3d origin, double range);

	void emitWorldwide(T signal, @NotNull World world);

	void emitGlobal(T signal, @NotNull MinecraftServer server);

	interface SubscriptionBuilder {
		@NotNull
		Subscription boundTo(@NotNull Entity entity);

		@NotNull
		Subscription boundTo(@NotNull BlockEntity blockEntity);

		@NotNull
		Subscription atPosition(@NotNull World world, @NotNull Vec3d pos);

		@NotNull
		Subscription worldwide(@NotNull World world);

		@NotNull
		Subscription global(MinecraftServer server);
	}

	interface Subscription {
		void cancel();
	}
}
