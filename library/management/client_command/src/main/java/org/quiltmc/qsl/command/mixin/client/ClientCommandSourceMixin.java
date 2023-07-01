/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.command.mixin.client;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

@Mixin(ClientCommandSource.class)
abstract class ClientCommandSourceMixin implements QuiltClientCommandSource {
	@Shadow
	@Final
	private MinecraftClient client;

	@Unique
	private final Map<String, Object> meta = new Object2ObjectOpenHashMap<>();

	@Override
	public void sendFeedback(Text message) {
		this.client.inGameHud.getChatHud().addMessage(message);
		this.client.getChatNarratorManager().narrate(message);
	}

	@Override
	public void sendError(Text message) {
		this.sendFeedback(message.copy().formatted(Formatting.RED));
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
