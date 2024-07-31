/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SendMessageFuncValue extends FuncValue {
	public static final Identifier TYPE = Identifier.of("quilt", "send_message");
	public static final MapCodec<SendMessageFuncValue> CODEC = Codec
			.STRING
			.fieldOf("message")
			.xmap(SendMessageFuncValue::new, sm -> sm.message);

	public static final PacketCodec<RegistryByteBuf, SendMessageFuncValue> PACKET_CODEC = PacketCodecs
			.STRING
			.<RegistryByteBuf>cast()
			.map(SendMessageFuncValue::new, sm -> sm.message);

	private final String message;

	public SendMessageFuncValue(String message) {
		super(TYPE);
		this.message = message;
	}

	@Override
	public void invoke(ServerPlayerEntity player) {
		player.sendMessage(Text.of("Quilt says: '" + this.message + "'"), false);
	}

	@Override
	public String toString() {
		return "send_message{'" + this.message + "'}";
	}
}
