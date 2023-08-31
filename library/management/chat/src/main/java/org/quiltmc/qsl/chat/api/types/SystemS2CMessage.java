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

package org.quiltmc.qsl.chat.api.types;

import java.util.EnumSet;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.text.Text;

import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalMessageTypesFactory;

/**
 * A wrapper around a system message. This will not display in chat if {@link #isOverlay()} is true, instead displaying over the actionbar.
 */
public class SystemS2CMessage extends AbstractChatMessage<SystemMessageS2CPacket> {
	private final Text content;
	private final boolean overlay;

	public SystemS2CMessage(PlayerEntity player, boolean isClient, SystemMessageS2CPacket packet) {
		this(
				player,
				isClient,
				packet.content(),
				packet.overlay()
		);
	}

	public SystemS2CMessage(PlayerEntity player, boolean isClient, Text content, boolean overlay) {
		super(player, isClient);
		this.content = content;
		this.overlay = overlay;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return InternalMessageTypesFactory.s2cType(QuiltMessageType.SYSTEM, this.isClient);
	}

	@Override
	public @NotNull SystemMessageS2CPacket serialized() {
		return new SystemMessageS2CPacket(this.content, this.overlay);
	}

	@Contract(pure = true)
	public Text getContent() {
		return this.content;
	}

	@Contract(pure = true)
	public boolean isOverlay() {
		return this.overlay;
	}

	@Contract(value = "_ -> new", pure = true)
	public SystemS2CMessage withContent(Text content) {
		return new SystemS2CMessage(this.player, this.isClient, content, this.overlay);
	}

	@Contract(value = "_ -> new", pure = true)
	public SystemS2CMessage withOverlay(boolean overlay) {
		return new SystemS2CMessage(this.player, this.isClient, this.content, overlay);
	}

	@Override
	public String toString() {
		return "SystemS2CMessage{" + "player=" + this.player +
				", content=" + this.content +
				", overlay=" + this.overlay +
				", player=" + this.player +
				", isClient=" + this.isClient +
				'}';
	}
}
