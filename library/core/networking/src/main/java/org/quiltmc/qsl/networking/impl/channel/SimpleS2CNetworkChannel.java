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

package org.quiltmc.qsl.networking.impl.channel;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.channel.S2CNetworkChannel;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public record SimpleS2CNetworkChannel<T>(
		Identifier id,
		NetworkCodec<T> codec,
		Supplier<Function<T, S2CNetworkChannel.Handler>> handlerProvider
) implements S2CNetworkChannel<T> {
	@Override
	public ClientPlayNetworking.ChannelReceiver createClientReceiver() {
		return (client, handler, buf, responseSender) -> {
			T message = this.codec.decode(buf);
			client.execute(() -> this.handlerProvider.get().apply(message).clientHandle(client, handler, responseSender));
		};
	}
}
