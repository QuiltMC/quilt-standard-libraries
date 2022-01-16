/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC, 2021 QuiltMC
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

package org.quiltmc.qsl.command.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientCommandSource.class)
abstract class ClientCommandSourceMixin implements QuiltClientCommandSource {
	@Shadow
	@Final
	private MinecraftClient client;

	@Unique private final Map<String, Object> meta = new HashMap<>();

	@Override
	public void sendFeedback(Text message) {
		this.client.inGameHud.addChatMessage(MessageType.SYSTEM, message, Util.NIL_UUID);
	}

	@Override
	public void sendError(Text message) {
		this.client.inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("").append(message).formatted(Formatting.RED), Util.NIL_UUID);
	}

	@Override
	public MinecraftClient getClient() {
		return this.client;
	}

	@Override
	public ClientPlayerEntity getPlayer() {
		return this.client.player;
	}

	@Override
	public ClientWorld getWorld() {
		return this.client.world;
	}

	@Override
	public Object getMeta(String key) {
		return this.meta.get(key);
	}

	@Override
	public void setMeta(String key, Object value) {
		this.meta.put(key, value);
	}
}
